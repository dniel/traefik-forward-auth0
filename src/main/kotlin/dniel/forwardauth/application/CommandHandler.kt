package dniel.forwardauth.application

interface Command {
}

interface CommandHandler<in T : Command> {
    fun handle(params: T): Event
}