package io.github.dgahn.akkadocs.user

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Scheduler
import akka.actor.typed.javadsl.AskPattern
import akka.http.javadsl.marshallers.jackson.Jackson
import akka.http.javadsl.model.StatusCodes
import akka.http.javadsl.server.Directives.*
import akka.http.javadsl.server.PathMatchers
import akka.http.javadsl.server.Route
import akka.japi.function.Function
import io.github.dgahn.akkadocs.user.UserRegistry.*
import java.time.Duration
import java.util.concurrent.CompletionStage


class UserRoutes(
    private val system: ActorSystem<*>,
    private val userRegistryActor: ActorRef<UserRegistry.Command?>
) {
    private val askTimeout: Duration? = Duration.ofMinutes(5)
    private val scheduler: Scheduler? = system.scheduler()

    private fun getUser(name: String): CompletionStage<GetUserResponse?>? {
        return AskPattern.ask<Command, GetUserResponse?>(
            userRegistryActor,
            Function { ref: ActorRef<GetUserResponse?>? -> GetUser(name, ref) },
            askTimeout,
            scheduler
        )
    }

    private fun deleteUser(name: String): CompletionStage<ActionPerformed?>? {
        return AskPattern.ask<Command, ActionPerformed?>(
            userRegistryActor,
            Function { ref: ActorRef<ActionPerformed?>? -> DeleteUser(name, ref) },
            askTimeout,
            scheduler
        )
    }

    private fun getUsers(): CompletionStage<Users?>? {
        return AskPattern.ask<Command, Users?>(
            userRegistryActor,
            Function { replyTo: ActorRef<Users?>? ->
                GetUsers(
                    replyTo
                )
            },
            askTimeout,
            scheduler
        )
    }

    private fun createUser(user: User): CompletionStage<ActionPerformed?>? {
        return AskPattern.ask<Command, ActionPerformed?>(
            userRegistryActor,
            Function { ref: ActorRef<ActionPerformed?>? -> CreateUser(user, ref) },
            askTimeout,
            scheduler
        )
    }

    fun userRoutes(): Route {
        return pathPrefix("users") {
            concat(
                //#users-get-delete
                pathEnd {
                    concat(
                        get {
                            onSuccess(getUsers()) { users: Users? ->
                                complete(StatusCodes.OK, users, Jackson.marshaller())
                            }
                        },
                        post {
                            entity(Jackson.unmarshaller(User::class.java)) { user: User? ->
                                onSuccess(createUser(user!!)) { performed: ActionPerformed? ->
                                    complete(StatusCodes.CREATED, performed, Jackson.marshaller())
                                }
                            }
                        })
                },
                path(PathMatchers.segment()) { name: String? ->
                    concat(
                        get {
                            rejectEmptyResponse {
                                onSuccess(getUser(name!!)) { performed: GetUserResponse? ->
                                    complete(StatusCodes.OK, performed!!.maybeUser, Jackson.marshaller())
                                }
                            }
                        },
                        delete {
                            onSuccess(deleteUser(name!!)) { performed: ActionPerformed? ->
                                complete(StatusCodes.OK, performed, Jackson.marshaller())
                            }
                        }
                    )
                } //#users-get-post
            )
        }
    }
}

