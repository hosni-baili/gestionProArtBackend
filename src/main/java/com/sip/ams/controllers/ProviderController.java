package com.sip.ams.controllers;
import java.util.List;
import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Random;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sip.ams.entities.Provider;
import com.sip.ams.repositories.ProviderRepository;

@RestController
@RequestMapping({"/providers","/hom*"})
@CrossOrigin(origins="*")
public class ProviderController {
	
	private final Path root = Paths.get(System.getProperty("user.dir") + "/src/main/resources/static/uploads");

	
	@Autowired
	private ProviderRepository providerRepository;
	
	@GetMapping("/list")
    public List<Provider> getAllProviders() {
        return (List<Provider>) providerRepository.findAll();
    }
	
	@PostMapping("/add")
	public Provider uplaodImage(@RequestParam("imageFile") MultipartFile file, @RequestParam("name") String name,
			@RequestParam("email") String email, @RequestParam("address") String address,
			@RequestParam("imageName") String imageName) throws IOException {

		String newImageName = getSaltString().concat(file.getOriginalFilename());
		try {
			Files.copy(file.getInputStream(),this.root.resolve(newImageName));
		} catch (Exception e) {
			throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
		}

		Provider provider = new Provider(name, address, email, newImageName);

		providerRepository.save(provider);
		return provider;
	}

	@PutMapping("/{providerId}")
	public Provider updateProvider(@PathVariable Long providerId, 
			
			@RequestParam("imageFile") MultipartFile file, @RequestParam("name") String name,
			@RequestParam("email") String email, @RequestParam("address") String address,
			@RequestParam("imageName") String imageName
			//@Valid @RequestBody Provider providerRequest
			
			
			) {
		return providerRepository.findById(providerId).map(provider -> {
			
			// STEP 1 : delete Old Image from server
			String OldImageName = provider.getNomImage();
					
				////////
					try {
						File f = new File(this.root + "/" + OldImageName); // file to be delete
						if (f.delete()) // returns Boolean value
						{
							System.out.println(f.getName() + " deleted"); // getting and printing the file name
						} else {
							System.out.println("failed");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

			 /////// END STEP 1
			
			/// STEP 2 : Upload new image to server
			String newImageName = getSaltString().concat(file.getOriginalFilename());
			try {
				Files.copy(file.getInputStream(), this.root.resolve(newImageName));
			} catch (Exception e) {
				throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
			}
			/// END STEP 2
			
			
			
			provider.setName(name);
			provider.setEmail(email);
			provider.setAddress(address);
			provider.setNomImage(newImageName);
			return providerRepository.save(provider);
		}).orElseThrow(() -> new IllegalArgumentException("ProviderId " + providerId + " not found"));
	}

	@DeleteMapping("/{providerId}")
	public ResponseEntity<?> deleteProvider(@PathVariable Long providerId) {
		return providerRepository.findById(providerId).map(provider -> {
			providerRepository.delete(provider);

			////////
			try {
				File f = new File(this.root + "/" + provider.getNomImage()); // file to be delete
				if (f.delete()) // returns Boolean value
				{
					System.out.println(f.getName() + " deleted"); // getting and printing the file name
				} else {
					System.out.println("failed");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			///////

			return ResponseEntity.ok().build();
		}).orElseThrow(() -> new IllegalArgumentException("ProviderId " + providerId + " not found"));
	}

	@GetMapping("/{providerId}")
	public Provider getProvider(@PathVariable Long providerId) {

		Optional<Provider> p = providerRepository.findById(providerId);

		return p.get();

	}

	// rundom string to be used to the image name
	protected static String getSaltString() {
		String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
		StringBuilder salt = new StringBuilder();
		Random rnd = new Random();
		while (salt.length() < 18) { // length of the random string.
			int index = (int) (rnd.nextFloat() * SALTCHARS.length());
			salt.append(SALTCHARS.charAt(index));
		}
		String saltStr = salt.toString();
		return saltStr;

	}

}
