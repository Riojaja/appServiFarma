package com.example.proyecto.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AppServifarmaBApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppServifarmaBApplication.class, args);
		System.out.println("Aplicación ServiFarma iniciada correctamente");
	}	 
}