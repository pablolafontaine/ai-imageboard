package com.pablolafontaine.imageboard.controller;


import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pablolafontaine.imageboard.service.ViewService;

@RestController
@RequestMapping("/")
public class ViewController {

    @Autowired
    private ViewService viewService;

    @GetMapping("/image/{id}")
    public ResponseEntity<String> viewImage(@PathVariable String id) {
        try {
            return viewService.viewImage(id).get();
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error viewing image: " + e.getMessage());
        }
    }

    @GetMapping("/{page}")
    public ResponseEntity<String> fetchIndexPage(@PathVariable int page) {
        try {
            return viewService.viewPage(page).get();
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error viewing image: " + e.getMessage());
        }
    }

    @GetMapping("/**")
    public ResponseEntity<String> notFound() {
        return ResponseEntity.notFound().build();
    }
}