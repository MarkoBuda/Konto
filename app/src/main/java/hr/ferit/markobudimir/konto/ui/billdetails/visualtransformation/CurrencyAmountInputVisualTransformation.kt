package hr.ferit.markobudimir.konto.ui.billdetails.visualtransformation

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.DecimalFormat

const val CURRENCY_PREFIX = "€"

class CurrencyAmountInputVisualTransformation(
    private val fixedCursorAtTheEnd: Boolean = true,
    private val numberOfDecimals: Int = 2
) : VisualTransformation {

    private val symbols = DecimalFormat().decimalFormatSymbols

    override fun filter(text: AnnotatedString): TransformedText {
        val thousandsSeparator = symbols.decimalSeparator
        val decimalSeparator = symbols.groupingSeparator
        val zero = symbols.zeroDigit

        val inputText = text.text

        val intPart = inputText
            .dropLast(numberOfDecimals)
            .reversed()
            .chunked(3)
            .joinToString(thousandsSeparator.toString())
            .reversed()
            .ifEmpty {
                zero.toString()
            }

        val fractionPart = inputText.takeLast(numberOfDecimals).let {
            if (it.length != numberOfDecimals) {
                List(numberOfDecimals - it.length) {
                    zero
                }.joinToString("") + it
            } else {
                it
            }
        }

        val formattedNumber = intPart + decimalSeparator + fractionPart

        val newText = AnnotatedString(
            text = CURRENCY_PREFIX + formattedNumber,
            spanStyles = text.spanStyles,
            paragraphStyles = text.paragraphStyles
        )

        val offsetMapping = if (fixedCursorAtTheEnd) {
            FixedCursorOffsetMapping(
                contentLength = inputText.length,
                formattedContentLength = formattedNumber.length
            )
        } else {
            MovableCursorOffsetMapping(
                unmaskedText = text.toString(),
                maskedText = newText.toString(),
                decimalDigits = numberOfDecimals
            )
        }

        return TransformedText(newText, offsetMapping)
    }
}
