package weblab4.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import weblab4.model.Point;
import weblab4.tools.ToolsHandler;

import java.util.*;

@RestController
@RequestMapping("")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class RequestController {


    @PostMapping("/api/register")
    public ResponseEntity<Map<String, String>> handleRegistrationEndPoint(
            @RequestBody Map<String, String> loginData) {
        String login = loginData.get("login");
        String password = loginData.get("password");
        // Assuming ToolsHandler.connectToDB.addUser returns a token
        String token = ToolsHandler.connectToDB.addUser(login, password);
        if(token.contains("Логин")){
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("token", token);
            return ResponseEntity.status(400).body(responseMap);
        }
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("token", token);
        return ResponseEntity.status(200).body(responseMap);
    }

    @PostMapping(value = {"/api/login"} )
    public ResponseEntity<Map<String, String>> handleLoginEndPoint(
            @RequestBody Map<String, String> loginData) {
        String user = loginData.get("login");
        String password = loginData.get("password");
        System.out.println(user + " " + password);
        // Проверка наличия пользователя в базе данных
        if (ToolsHandler.connectToDB.userValid(user, password)) {

            // Получение токена для пользователя
            String token = ToolsHandler.connectToDB.getTokenForUser(user);
            // Возвращаем успешный ответ с токеном
            return ResponseEntity.status(200).body(Map.of("token", token));
        } else {
            // Возвращаем сообщение об ошибке
            return ResponseEntity.status(400).body(Map.of("token", "Неверные логин или пароль"));
        }
    }

    @PostMapping(value = {"/api/checkToken"} )
    public ResponseEntity<Map<String, String>> checkToken(
            @RequestBody Map<String, String> loginData) {
        String token = loginData.get("token");
        if(token == null){
            return ResponseEntity.status(400).build();
        }
        // Проверка наличия пользователя в базе данных
        if (ToolsHandler.connectToDB.checkToken(token)) {
            // Возвращаем успешный ответ с токеном
            return ResponseEntity.status(200).build();
        } else {
            // Возвращаем сообщение об ошибке
            return ResponseEntity.status(400).build();
        }
    }

    @PostMapping(value = {"/api/getPoints"} )
    public ResponseEntity<String> getPoints(
            @RequestBody Map<String, String> loginData) {
        String token = loginData.get("token");
        System.out.println(token);
        if(ToolsHandler.connectToDB.getPoints(token).isEmpty()){
            System.out.println("1");
            return ResponseEntity.status(200).body("{}");
        }
        List<Point> points = ToolsHandler.connectToDB.getPoints(token);
        System.out.println(points.toString());
        if(points == null|| points.isEmpty()){
            return ResponseEntity.status(400).body("");
        }
        if (points != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonPoints = "";
            try {
                jsonPoints = objectMapper.writeValueAsString(points);
            }catch (Exception e){
                System.out.println("Error here");
            }
            System.out.println(jsonPoints);
            return ResponseEntity.status(200).body(jsonPoints);
        } else {
            // Return an empty list if no points are found or handle the case accordingly
            return ResponseEntity.status(400).body("");
        }
    }

    @PostMapping(value = {"/api/clear"} )
    public ResponseEntity<String> clear(
            @RequestBody Map<String, String> loginData) {
        String token = loginData.get("token");
        if(!ToolsHandler.connectToDB.checkToken(token)){
            return ResponseEntity.status(400).body("");
        }
        ToolsHandler.connectToDB.clear(token);
        return ResponseEntity.status(200).body("");
    }

    @PostMapping(value = {"/api/addPoint"} )
    public ResponseEntity<String> addPoint(
            @RequestBody Map<String, String> attempData) throws JsonProcessingException {
        Double x = Double.parseDouble(attempData.get("x"));
        Double y = Double.parseDouble(attempData.get("y"));
        Double r = Double.parseDouble(attempData.get("r"));
        String token = attempData.get("token");
        Point point = new Point();
        point.setX(x);
        point.setY(y);
        point.setR(r);
        point.check();
        point.setOwner(token);
        ToolsHandler.connectToDB.addPointToTable(point);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPoint = objectMapper.writeValueAsString(point);
        return ResponseEntity.status(200).body(jsonPoint);
    }
}