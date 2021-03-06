package com.islamversity.core.mvi

import com.islamversity.core.FlowBlock
import com.islamversity.core.publish
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


/**
 * Object that will subscribes to a [MviView]'s [MviIntent]s,
 * process it and emit a [MviViewState] back.
 *
 * @param I Top class of the [MviIntent] that the [MviPresenter] will be subscribing
 * to.
 * @param S Top class of the [MviViewState] the [MviPresenter] will be emitting.
 */
interface MviPresenter<I : MviIntent, S : MviViewState> {

    fun processIntents(intents: I)

    fun states(): Flow<S>

    fun close() {
    }
}

abstract class BasePresenter<I : MviIntent, S : MviViewState, R : MviResult>(
    val processor: MviProcessor<I, R>,
    private val startState: S
) : MviPresenter<I, S> {


    private val presenterJob = SupervisorJob()
    protected val uiScope = CoroutineScope(Dispatchers.Main + presenterJob)
    private val holder = StatePublisher(startState, uiScope)

    private val reducer: suspend (S, R) -> S = { preState, result ->
        reduce(preState, result)
    }

    private val intents = Channel<I>(Channel.UNLIMITED)
    private val states: Flow<S> = compose()

    private fun compose(): Flow<S> =
        intents
            .receiveAsFlow()
            .publish(filterIntent())
            .let(processor.actionProcessor)
            .scan(startState, { preState, result ->
                reduce(preState, result)
            })
            .distinctUntilChanged()
            .onEach {
                logNewState(it)
            }

    init {
        holder.start(states)
    }

    override fun processIntents(intents: I) {
        if (!this.intents.isClosedForSend) {
            this.intents.offer(intents)
        }
    }

    override fun states(): Flow<S> = holder.receiveStates()

    protected open suspend fun logNewState(newState: S) {

    }

    protected open fun filterIntent(): List<FlowBlock<I, I>> =
        listOf<FlowBlock<I, I>>({
            this
        })

    protected abstract fun reduce(preState: S, result: R): S

    override fun close() {
        intents.cancel()
        holder.close()
        presenterJob.cancel()
    }


    class StatePublisher<S : MviViewState>(
        private var latestState: S,
        private val scope: CoroutineScope
    ) {

        private val lock = Mutex()
        private val channelList = mutableListOf<Channel<S>>()

        fun start(states: Flow<S>) {
            states
                .onEach {
                    lock.withLock {
                        latestState = it
                        signalListeners()
                    }
                }
                .onCompletion {
                    closeAllChannels()
                }
                .launchIn(scope)
        }

        private fun closeAllChannels() {
            channelList.toList().forEach { channel ->
                channelList.remove(channel)
                channel.cancel()
            }
        }

        private fun signalListeners() {
            channelList.forEach { it.offer(latestState) }
        }

        fun receiveStates(): Flow<S> {
            val newChannel = Channel<S>(Channel.UNLIMITED)
            channelList += newChannel

            return newChannel.consumeAsFlow()
                .onCompletion {
                    if (channelList.remove(newChannel)) {
                        newChannel.cancel()
                    }
                    println("checked closed $newChannel")
                }.also {
                    newChannel.offer(latestState)
                }
        }

        fun close() {
            closeAllChannels()
        }
    }

}
