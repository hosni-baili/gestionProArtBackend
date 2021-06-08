package com.sip.ams;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.sip.ams.services.FilesStorageService;

import javax.annotation.Resource;

import org.springframework.boot.CommandLineRunner;

@SpringBootApplication
public class AmsApiApplication implements CommandLineRunner {
	 @Resource
	  FilesStorageService storageService;
	public static void main(String[] args) {
		SpringApplication.run(AmsApiApplication.class, args);
	}
	@Override
	  public void run(String... arg) throws Exception {
	    storageService.deleteAll();
	    storageService.init();
	  }
}
