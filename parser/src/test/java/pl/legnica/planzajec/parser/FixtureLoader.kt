package pl.legnica.planzajec.parser

import java.io.InputStreamReader
import java.nio.charset.Charset

object FixtureLoader {
    fun load(fixtureName: String): String {
        val inputStream = javaClass.classLoader.getResourceAsStream("fixtures/$fixtureName")
            ?: error("Fixture $fixtureName not found in resources/fixtures/")
        return InputStreamReader(inputStream, Charset.forName("ISO-8859-2")).use { it.readText() }
    }
}
