package be.pjrotsaert.mijnhogent.api

import android.content.Context

// Describes an API Error, name and description are resource ids that point to strings
class APIError(
        private val name: Int,
        private val description: Int) {

    fun getNameId(): Int {
        return name
    }
    fun getDescriptionId(): Int {
        return description
    }

    fun getName(ctx: Context): String {
        return ctx.resources.getString(name)
    }
    fun getDescription(ctx: Context): String {
        return ctx.resources.getString(description)
    }
}