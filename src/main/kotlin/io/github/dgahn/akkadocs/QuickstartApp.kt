package io.github.dgahn.akkadocs

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.actor.typed.ActorRef
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Adapter
import akka.actor.typed.javadsl.Behaviors
import akka.http.javadsl.ConnectHttp
import akka.http.javadsl.Http
import akka.http.javadsl.ServerBinding
import akka.http.javadsl.model.HttpRequest
import akka.http.javadsl.model.HttpResponse
import akka.http.javadsl.server.Route
import akka.stream.Materializer
import akka.stream.javadsl.Flow
import io.github.dgahn.akkadocs.user.UserRegistry
import io.github.dgahn.akkadocs.user.UserRoutes
import mu.KotlinLogging
import java.net.InetSocketAddress
import java.util.concurrent.CompletionStage


class QuickstartApp {

    private val log = KotlinLogging.logger {}

    fun startHttpServer(
        route: Route,
        system: ActorSystem<*>
    ) {
        val classicSystem: akka.actor.ActorSystem = Adapter.toClassic(system)
        val http: Http = Http.get(classicSystem)
        val materializer: Materializer = Materializer.matFromSystem(system)
        val routeFlow: Flow<HttpRequest, HttpResponse, NotUsed> = route.flow(classicSystem, materializer)
        val futureBinding: CompletionStage<ServerBinding> =
            http.bindAndHandle(routeFlow, ConnectHttp.toHost("localhost", 8080), materializer)
        futureBinding.whenComplete { binding: ServerBinding?, exception: Throwable? ->
            if (binding != null) {
                val address: InetSocketAddress = binding.localAddress()
                log.info { "Server online at http://${address.hostString}:${address.port}" }
            } else {
                log.error{"Failed to bind HTTP endpoint, terminating system : $exception"}
                system.terminate()
            }
        }
    }
}

fun main() {
    val rootBehavior =
        Behaviors.setup { context: ActorContext<NotUsed?> ->
            val userRegistryActor: ActorRef<UserRegistry.Command?> =
                context.spawn(UserRegistry.create(), "UserRegistry")
            val userRoutes = UserRoutes(context.system, userRegistryActor)
            QuickstartApp().startHttpServer(userRoutes.userRoutes(), context.system)
            Behaviors.empty()
        }

    ActorSystem.create(rootBehavior, "HelloAkkaHttpServer");
}


