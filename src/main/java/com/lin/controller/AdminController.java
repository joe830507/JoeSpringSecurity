package com.lin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/admin/api")
public class AdminController {

	@GetMapping(value = "hello")
	public String hello() {
		return "Hello,admin";
	}
}
