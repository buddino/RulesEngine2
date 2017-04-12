package it.cnit.gaia.rulesengine;


import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class Application implements CommandLineRunner {

	public static void main(final String[] args) {
		System.out.println("   _____          _____          \n" +
				"  / ____|   /\\   |_   _|   /\\    \n" +
				" | |  __   /  \\    | |    /  \\   \n" +
				" | | |_ | / /\\ \\   | |   / /\\ \\  \n" +
				" | |__| |/ ____ \\ _| |_ / ____ \\  Rules\n" +
				"  \\_____/_/    \\_\\_____/_/    \\_\\ Engine\n" +
				"     \u001B[32mGREEN AWARENESS IN ACTION\u001B[0m");
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(String... strings) throws Exception {
	}


}