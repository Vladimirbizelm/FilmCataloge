package com.example.filmcataloge.utils


/**
 * Обертка для событий, которые должны обрабатываться только один раз
 * Предотвращает повторное срабатывание при пересоздании UI компонентов
 */
class Event<T>(private val content: T) {
    private var hasBeenHandled = false

    /**
     * Возвращает содержимое, если оно еще не было обработано
     * иначе возвращает null
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Возвращает содержимое, даже если оно уже было обработано
     */
    fun peekContent(): T = content
}