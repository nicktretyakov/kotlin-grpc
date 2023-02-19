package customer_service.api

import io.grpc.Status
import io.grpc.examples.check.GreeterCoroutineGrpc
import io.grpc.examples.check.CheckReply
import io.grpc.examples.check.CheckRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.toList
import kotlin.coroutines.CoroutineContext

class GreeterService : GreeterCoroutineGrpc.GreeterImplBase() {

    val myThreadLocal = ThreadLocal.withInitial { "value" }.asContextElement()

    override val initialContext: CoroutineContext
        get() = Dispatchers.Default + myThreadLocal


    private val validNameRegex = Regex("[^0-9]*")

    override suspend fun sayCheck(request: CheckRequest): CheckReply  {

        if (request.name.matches(validNameRegex)) {
            return CheckReply.newBuilder()
                .setMessage("Check there, ${request.name}!")
                .build()
        } else {
            throw Status.INVALID_ARGUMENT.asRuntimeException()
        }
    }

    override suspend fun sayCheckStreaming(
        requestChannel: ReceiveChannel<CheckRequest>,
        responseChannel: SendChannel<CheckReply>
    ) {
        requestChannel.consumeEach { request ->

            responseChannel
                .send { message = "Check there, ${request.name}!" }
        }
    }

    override suspend fun sayCheckClientStreaming(
        requestChannel: ReceiveChannel<CheckRequest>
    ): CheckReply {

        return CheckReply.newBuilder()
            .setMessage(requestChannel.toList().joinToString())
            .build()
    }

    override suspend fun sayCheckServerStreaming(request: CheckRequest, responseChannel: SendChannel<CheckReply>) {

        for(char in request.name) {
            responseChannel.send {
                message = "Check $char!"
            }
        }
    }
}