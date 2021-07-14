package auth

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    @Suppress("SpreadOperator")
    SpringApplication.run(Application::class.java, *args)
}
