package dniel.forwardauth.application

interface Event {
}

interface Command {
}

interface CommandHandler<in T : Command> {
    fun handle(params: T): List<Event>
}