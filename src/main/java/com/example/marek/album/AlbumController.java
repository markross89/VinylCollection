package com.example.marek.album;


import com.example.marek.image.ImageRepository;
import com.example.marek.track.TrackRepository;
import com.example.marek.user.CurrentUser;
import com.example.marek.user.User;
import com.example.marek.user.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/album")
public class AlbumController {
	
	
	private final AlbumRepository albumRepository;
	private final ImageRepository imageRepository;
	private final TrackRepository trackRepository;
	private final UserRepository userRepository;
	
	public AlbumController (AlbumRepository albumRepository, ImageRepository imageRepository, TrackRepository trackRepository,
							UserRepository userRepository) {
		
		this.albumRepository = albumRepository;
		this.imageRepository = imageRepository;
		this.trackRepository = trackRepository;
		this.userRepository = userRepository;
	}
	
	
	@GetMapping("/albums")
	public String display (Model model, @AuthenticationPrincipal CurrentUser customUser) throws JsonProcessingException {
		
		List<Album> albums = albumRepository.findByUsersContains(customUser.getUser());
		List<Map<String, String>> thumbs = new ArrayList<>();
		for (Album a : albums) {
			Map<String, String> map = new HashMap<>();
			map.put("id", String.valueOf(a.getDiscogsId()));
			map.put("title", String.join("-", a.getArtist(), a.getTitle()));
			map.put("image", a.getImages().get(0).getUri());
			thumbs.add(map);
		}
		model.addAttribute("thumbs", thumbs);
		
		return "album/albums";
	}
	
	@GetMapping("/remove/{id}")
	public String remove (@PathVariable long id, @AuthenticationPrincipal CurrentUser customUser) {
		
		Album album = albumRepository.findAlbumByDiscogsId(id);
		albumRepository.deleteUserAlbumConstrains(album.getId(), customUser.getUser().getId());
		List<User> users = album.getUsers();
		if (users.isEmpty()) {
			albumRepository.deleteAlbumByDiscogsId(id);
		}
		return "redirect:/album/albums";
	}
	
	
}
