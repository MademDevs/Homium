package de.madem.homium.errors.presentationerrors

import androidx.annotation.StringRes
import de.madem.homium.R

sealed class ValidationException(@StringRes val errMsgResId : Int): Exception() {
    object InvalidParametersException : ValidationException(R.string.errormsg_invalid_parameters)
}