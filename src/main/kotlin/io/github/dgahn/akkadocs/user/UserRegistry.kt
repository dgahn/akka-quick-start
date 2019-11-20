package io.github.dgahn.akkadocs.user

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*


class UserRegistry private constructor(context: ActorContext<Command?>?) :
    AbstractBehavior<UserRegistry.Command?>(context) {
    // actor protocol
    interface Command

    class GetUsers(val replyTo: ActorRef<Users?>?) :
        Command {

    }

    class CreateUser(val user: User?, val replyTo: ActorRef<ActionPerformed?>?) :
        Command {

    }

    class GetUserResponse(val maybeUser: Optional<User?>?)

    class GetUser(val name: String?, val replyTo: ActorRef<GetUserResponse?>?) :
        Command {

    }

    class DeleteUser(val name: String?, val replyTo: ActorRef<ActionPerformed?>?) :
        Command {

    }

    class ActionPerformed(val description: String?) : Command

    class User @JsonCreator constructor(
        @param:JsonProperty("name") val name: String?,
        @param:JsonProperty("age") val age: Int,
        @param:JsonProperty("countryOfRecidence") val countryOfResidence: String?
    )

    class Users(val users: MutableList<User?>?)

    private val users: MutableList<User?>? = ArrayList()
    override fun createReceive(): Receive<Command?>? {
        return newReceiveBuilder()
            .onMessage(
                GetUsers::class.java
            ) { command: GetUsers? -> onGetUsers(command) }
            .onMessage(
                CreateUser::class.java
            ) { command: CreateUser? -> onCreateUser(command) }
            .onMessage(GetUser::class.java) { command: GetUser? -> onGetUser(command) }
            .onMessage(
                DeleteUser::class.java
            ) { command: DeleteUser? -> onDeleteUser(command) }
            .build()
    }

    private fun onGetUsers(command: GetUsers?): Behavior<Command?>? {
        command!!.replyTo!!.tell(
            Users(
                Collections.unmodifiableList(
                    ArrayList(users)
                )
            )
        )
        return this
    }

    private fun onCreateUser(command: CreateUser?): Behavior<Command?>? {
        users!!.add(command!!.user)
        command.replyTo!!.tell(
            ActionPerformed(
                String.format(
                    "User %s created.",
                    command.user!!.name
                )
            )
        )
        return this
    }

    private fun onGetUser(command: GetUser?): Behavior<Command?>? {
        val maybeUser = users!!.stream()
            .filter { user: User? -> user!!.name == command!!.name }
            .findFirst()
        command!!.replyTo!!.tell(GetUserResponse(maybeUser))
        return this
    }

    private fun onDeleteUser(command: DeleteUser?): Behavior<Command?>? {
        users!!.removeIf { user: User? -> user!!.name == command!!.name }
        command!!.replyTo!!.tell(
            ActionPerformed(
                String.format(
                    "User %s deleted.",
                    command.name
                )
            )
        )
        return this
    }

    companion object {
        fun create(): Behavior<Command?>? {
            return Behaviors.setup { context: ActorContext<Command?>? ->
                UserRegistry(
                    context
                )
            }
        }
    }
}