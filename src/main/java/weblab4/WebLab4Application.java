package weblab4;

import org.springframework.beans.factory.annotation.Value;
import weblab4.dbmanager.ConnectToDB;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import weblab4.tools.ToolsHandler;

@SpringBootApplication
public class WebLab4Application {


    public static void main(String[] args) {
        ConnectToDB connectToDB = new ConnectToDB();
        try {
            connectToDB.init();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        ToolsHandler.connectToDB = connectToDB;
        SpringApplication.run(WebLab4Application.class, args);

    }

}