package it.fm3.alcolist.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import it.fm3.alcolist.service.StatisticsServiceI;

@RestController
@CrossOrigin
@RequestMapping("manage-statistics")
public class StatisticsController {
	
	@Autowired
	StatisticsServiceI statisticsService;
	
	
	@RequestMapping(value = "/getNumbersOfUsers/{role}", method = RequestMethod.GET)
	public ResponseEntity<?> getByRoleUsers(@PathVariable(name = "role") String role) throws Exception {
		return ResponseEntity.ok(statisticsService.getByNumbersOfUsers(role));
	}
	
	@RequestMapping(value = "/getNumbersOfCreatedByUserUuid/{uuid}", method = RequestMethod.GET)
	public ResponseEntity<?> getByCreatedBy(@PathVariable(name = "uuid") String uuid) throws Exception {
		return ResponseEntity.ok(statisticsService.getNumbersOfCreatedByUser(uuid));
	}
	
	@RequestMapping(value = "/getNumbersOfDeliveredByUserUuid/{uuid}", method = RequestMethod.GET)
	public ResponseEntity<?> getByDeliveredBy(@PathVariable(name = "uuid") String uuid) throws Exception {
		return ResponseEntity.ok(statisticsService.getNumbersOfDeliveredByUser(uuid));
	}
	
	@RequestMapping(value = "/getNumbersOfExecutedByUserUuid/{uuid}", method = RequestMethod.GET)
	public ResponseEntity<?> getByExecutedBy(@PathVariable(name = "uuid") String uuid) throws Exception {
		return ResponseEntity.ok(statisticsService.getNumbersOfExecutedByUser(uuid));
	}
	
}