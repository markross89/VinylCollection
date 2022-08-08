package com.example.marek;

import com.example.marek.album.Album;
import com.example.marek.album.AlbumDao;
import com.example.marek.album.AlbumRepository;
import com.example.marek.image.Image;
import com.example.marek.image.ImageDao;
import com.example.marek.track.Track;
import com.example.marek.track.TrackDao;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;


@Controller
@RequestMapping("/home")
public class HomeController {
	
	
	private static final String DISCOGS_NEW_RELASE = "https://api.discogs.com/database/search?q=new&type=release&format_exact=Vinyl&";
	private static final String DISCOGS_KEY_SECRET = "key=RqgTChKtuwVLyeWqVgFL&secret=sTkBKTNnXoSjWCnVRIOabYoBZUsDgzmY";
	private static final String DISCOGS_ALBUM = "https://api.discogs.com/releases/";
	private static final String DISCOGS_SEARCH = "https://api.discogs.com/database/search?q=";
	
	private final ApiController apiController;
	private final TrackDao trackDao;
	private final AlbumDao albumDao;
	private final ImageDao imageDao;
	private final AlbumRepository albumRepository;
	
	public HomeController (ApiController apiController, TrackDao trackDao, AlbumDao albumDao, ImageDao imageDao, AlbumRepository albumRepository) {
		
		this.apiController = apiController;
		this.trackDao = trackDao;
		this.albumDao = albumDao;
		this.imageDao = imageDao;
		this.albumRepository = albumRepository;
	}
	
	
	@GetMapping("/start")
	public String start (Model model) throws JsonProcessingException {
		
		Map map = apiController.mapRequestData(String.join("", DISCOGS_NEW_RELASE, DISCOGS_KEY_SECRET));
		model.addAttribute("thumbs", apiController.thumbsDisplay(map));
		return "home/start";
	}
	
	@PostMapping("/start")
	public String searchDiscogs (@RequestParam String dada, Model model) throws JsonProcessingException {
		
		Map map = apiController.mapRequestData(String.join("", DISCOGS_SEARCH, dada, "&", DISCOGS_KEY_SECRET));
		model.addAttribute("thumbs", apiController.thumbsDisplay(map));
		return "home/searchDetails";
	}

	@GetMapping("/details/{id}")
	public String albumDetails (Model model, @PathVariable long id) throws JsonProcessingException {
		
		Map map = apiController.mapRequestData(String.join("", DISCOGS_ALBUM, String.valueOf(id), "?", DISCOGS_KEY_SECRET));
		model.addAttribute("albumDetails", apiController.getAlbumDetails(map));
		model.addAttribute("tracklist", apiController.getTracklist(map));
		model.addAttribute("images", apiController.getImages(map));
		
		return "home/details";
	}
	
	
	
	@GetMapping("/add/{id}")
	public String addAlbum (@PathVariable long id) throws JsonProcessingException {
		
		if (albumRepository.findAlbumByDiscogsId(id).isPresent()) {
			return "home/start";
		}
		else {
			
			Map map = apiController.mapRequestData(String.join("", DISCOGS_ALBUM, String.valueOf(id), "?", DISCOGS_KEY_SECRET));
			
			List<Track> tracks = new ArrayList<>();
			for (Object o : apiController.getAlbumTracklist(map)) {
				Track track = new Track();
				track.setPosition(apiController.getTracklistSongDetail(o, "position"));
				track.setTitle(apiController.getTracklistSongDetail(o, "title"));
				track.setDuration(apiController.getTracklistSongDetail(o, "duration"));
				tracks.add(track);
				trackDao.save(track);
			}
			List<Image> images = new ArrayList<>();
			for (Object o : apiController.getAlbumImages(map)) {
				Image image = new Image();
				image.setType(apiController.getAlbumImageDetail(o, "type"));
				image.setUri(apiController.getAlbumImageDetail(o, "uri"));
				images.add(image);
				imageDao.save(image);
			}
			
			Album album = new Album();
			LocalDate date = LocalDate.now();
			album.setDiscogsId(id);
			album.setArtist(apiController.getAlbumArtist(map));
			album.setTitle(apiController.getAlbumTitle(map));
			album.setLabel(apiController.getAlbumLabel(map));
			album.setCatno(apiController.getAlbumCatno(map));
			album.setUri(apiController.getAlbumUri(map));
			album.setGenre(apiController.getAlbumGenre(map));
			album.setImages(images);
			album.setTracks(tracks);
			album.setDate(Date.valueOf(date));
			albumDao.save(album);
			
		}
		return "home/start";
	}

}
