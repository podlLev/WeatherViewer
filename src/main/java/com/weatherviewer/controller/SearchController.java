package com.weatherviewer.controller;

import com.weatherviewer.dto.AddLocationDto;
import com.weatherviewer.dto.GeoLocationDto;
import com.weatherviewer.security.SecUser;
import com.weatherviewer.service.LocationService;
import com.weatherviewer.service.WeatherApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final WeatherApiService weatherApiService;
    private final LocationService locationService;
    private final Validator validator;

    @GetMapping("/search")
    public String searchResults(@RequestParam("q") String query, Model model,
                                @AuthenticationPrincipal SecUser user) {
        log.info("User '{}' is searching for locations with query='{}'", user.getUsername(), query);
        List<GeoLocationDto> foundLocations = weatherApiService.getCitiesByName(query);

        model.addAttribute("foundLocations", foundLocations);
        model.addAttribute("addLocation", new AddLocationDto());
        log.info("Search returned {} results for user '{}'", foundLocations.size(), user.getUsername());

        model.addAttribute("login", user.getFullName());
        return "search";
    }

    @PostMapping("/search/add")
    public String addLocation(@ModelAttribute("addLocation") AddLocationDto addLocationDto,
                              @AuthenticationPrincipal SecUser secUser,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        addLocationDto.setUserId(secUser.getId());
        validator.validate(addLocationDto, bindingResult);

        if (bindingResult.hasErrors()) {
            List<String> errorMessages = bindingResult.getAllErrors().stream()
                    .map(ObjectError::getDefaultMessage)
                    .collect(Collectors.toList());
            log.info("Failed to add location for user '{}': {}", secUser.getUsername(), errorMessages);

            redirectAttributes.addFlashAttribute("errorMessages", errorMessages);
            return "redirect:/";
        }

        locationService.add(addLocationDto);
        log.info("Location '{}' added successfully for user '{}'", addLocationDto.getName(), secUser.getUsername());
        redirectAttributes.addFlashAttribute("successMessage", "Location added successfully!");
        return "redirect:/";
    }

}
