package dniel.forwardauth.application

interface Event {

    /**
     * return the name of the event as field type.
     */
    fun type(): String {
        return this.javaClass.simpleName
    }
}


interface EventHandler<in T : Event> {
    fun handle(params: T): Event
}