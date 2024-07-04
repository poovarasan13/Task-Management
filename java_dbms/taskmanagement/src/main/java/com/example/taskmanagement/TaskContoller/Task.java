package com.example.taskmanagement.TaskContoller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.util.HashMap;


@Controller
public class Task {


    String jdbcurl = "jdbc:mysql://127.0.0.1:3306/taskmanager";
    String username;
    String teamid;


    @GetMapping("/start")
    public String start()
    {
        return "login";
    }
    @GetMapping ("/sign")
    public String dashboard()
    {
        return "signup";
    }
    @PostMapping("/signup")
    public  String signup(@RequestParam("signupusername")String name, @RequestParam("signuppassword") String password)
    {
        System.out.println("Inside signup");

        Connection connection = null;

        try{
            connection = DriverManager.getConnection(jdbcurl,"root","poovarasan@13");
            String sql= "insert into user values(?,?)";
            PreparedStatement statement= connection.prepareStatement(sql);
            //when we use varialbles we use prepare statement or we use statement/
            statement.setString(1,name);/* to replace the quetion mark we use preaparestatement and set the value to ?*/
            statement.setString(2,password);
            statement.executeUpdate();


        }
        catch (Exception e){
            System.out.println(e);
        }
        return "login";
    }
    @PostMapping("/login")
       public String login(Model model, @RequestParam("loginusername") String name, @RequestParam("loginpassword")String password){
        Connection connection=null;
        this.username=name;
        try{
            connection =DriverManager.getConnection(jdbcurl,"root","poovarasan@13");
            String sql="SELECT password from user WHERE name=?";
            PreparedStatement statement= connection.prepareStatement(sql);
            statement.setString(1,name);
            ResultSet rs=statement.executeQuery();
            while(rs.next())
            {
                if(password.equals(rs.getString("password"))){
                    return "mainpage";
                }
                else{
                    return "alert";
                }
            }
        }
        catch(SQLException e)
        {
            System.out.println(e);
        }



        return "login";
    }
    @PostMapping ("/join")
    public String join()
    {
        return "join";
    }

    @PostMapping("/viewtask")
    public String viewTask(@RequestParam("jointeamid") String teamid) {
        this.teamid = teamid;
        System.out.println(teamid);
        int lastValue = 0;
        Connection connection = null;
        PreparedStatement statementCheck = null;
        PreparedStatement statement = null;
        try {
            connection = DriverManager.getConnection(jdbcurl, "root", "poovarasan@13");

            String sqlCheck = "SELECT COUNT(*) FROM " + teamid + " WHERE username = ?";
            statementCheck = connection.prepareStatement(sqlCheck);
            statementCheck.setString(1, this.username);
            ResultSet rsCheck = statementCheck.executeQuery();
            if (rsCheck.next() && rsCheck.getInt(1) > 0) {
                System.out.println("Username already exists in the table");
                return "justview";
            } else {
                String sql = "SELECT MAX(teamno) FROM " + teamid;
                statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    lastValue = rs.getInt(1) + 1;
                }
                String sqlInsert = "INSERT INTO " + teamid + " VALUES (?, ?, ?)";
                PreparedStatement statement1 = connection.prepareStatement(sqlInsert);
                statement1.setString(1, teamid);
                statement1.setString(2, this.username);
                statement1.setString(3, String.valueOf(lastValue));
                statement1.executeUpdate();

            }
        } catch (SQLException e) {
            System.out.println(e);
        } finally {
            try {
                if (statementCheck != null) {
                    statementCheck.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
        return "justview";
    }

    @PostMapping("/create")
    public String createteam()
    {
        return "create";
    }

    @PostMapping("/createteam")
    public String createteam( @RequestParam("createteamid") String teamid){
            this.teamid=teamid;
        Connection connection=null;

        try{
            connection=DriverManager.getConnection(jdbcurl,"root","poovarasan@13");
            String sql="create table "+teamid+"(teamid varchar(20),username varchar(20),teamno int)";
            PreparedStatement statement=connection.prepareStatement(sql);

            String insertsql="insert into "+teamid+" values(?,?,?)";
            PreparedStatement statement1=connection.prepareStatement(insertsql);
            statement1.setString(1,teamid);
            statement1.setString(2,this.username);
            statement1.setString(3, String.valueOf(1));

            statement.executeUpdate();
            statement1.executeUpdate();

            System.out.println("table created");
                 return "view";

        }
        catch (Exception e){
            System.out.println(e);
        }
        System.out.println("in this name table is already craeated");

        return "create";

    }

    @GetMapping("/justview")

    public String justview() {
        return "justview";
    }

//    @PostMapping("/justviewtask")
//    public String justviewtask()
//    {
//        return "view";
//    }

    @PostMapping("/justviewtask")
    public String justviewtask(Model model,@RequestParam("teamid") String teamid) {
        this.teamid=teamid;
        System.out.println(teamid);
        Connection connection=null;
        try {
            connection = DriverManager.getConnection(jdbcurl, "root", "poovarasan@13");
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, null, teamid + "task", null);
            if (!tables.next()) {
                String sql = "create table  " + teamid + "task (sender varchar(20), task varchar(100), enddate date, receiver varchar(20),progress int)";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.execute();
                return "justview";
            }

        }
            catch(SQLException e)
            {
                System.out.println(e);
            }


      List<Map<String,Object>> data=fetchTask(teamid);
      model.addAttribute("taskdetails",data);

        List<Map<String, Object>> data2 = fetchTask2(teamid);
        model.addAttribute("taskdetails2", data2);

        List<Map<String, Object>> data3 = fetchTask3(teamid);
        model.addAttribute("taskdetails3", data3);

        return "view";

    }

@ModelAttribute("taskdetails")
    public List<Map<String,Object>> fetchTask(String teamid)
{
    List<Map<String,Object>> listofdetails=new ArrayList<>();
    Connection connection=null;
    try {
        connection = DriverManager.getConnection(jdbcurl, "root", "poovarasan@13");


        String sql = "Select * from "+teamid+"task order by progress desc";
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet rs = statement.executeQuery();
        while (rs.next()) {
            Map<String, Object> mp = new HashMap<>();
            mp.put("Sender", rs.getString("sender"));
            mp.put("Task", rs.getString("task"));
            mp.put("Receiver", rs.getString("receiver"));
            mp.put("Enddate", rs.getDate("enddate"));
            mp.put("Progress", rs.getInt("progress"));
            listofdetails.add(mp);

        }

    }
    catch (SQLException e)
    {
        System.out.println(e);
    }
return listofdetails;


}

    @ModelAttribute("taskdetails2")
    public List<Map<String, Object>> fetchTask2(String teamid) {
        List<Map<String, Object>> listofdetails = new ArrayList<>();
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(jdbcurl, "root", "poovarasan@13");

            // Example SQL query for fetching specific tasks
            String sql = "SELECT * FROM " + teamid + "task WHERE receiver='"+this.username+"'";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Map<String, Object> mp = new HashMap<>();
                mp.put("Sender", rs.getString("sender"));
                mp.put("Task", rs.getString("task"));
                mp.put("Receiver", rs.getString("receiver"));
                mp.put("Enddate", rs.getDate("enddate"));
                mp.put("Progress", rs.getInt("progress"));
                listofdetails.add(mp);
            }
            System.out.println(listofdetails);
        } catch (SQLException e) {
            System.out.println(e);
        }
        return listofdetails;
    }

    @ModelAttribute("taskdetails3")
    public List<Map<String, Object>> fetchTask3(String teamid) {
        List<Map<String, Object>> listofdetails = new ArrayList<>();
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(jdbcurl, "root", "poovarasan@13");

            // Example SQL query for fetching specific tasks
            String sql = "SELECT * FROM " + teamid + "task WHERE sender='"+this.username+"'";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Map<String, Object> mp = new HashMap<>();
                mp.put("Sender", rs.getString("sender"));
                mp.put("Task", rs.getString("task"));
                mp.put("Receiver", rs.getString("receiver"));
                mp.put("Enddate", rs.getDate("enddate"));
                mp.put("Progress", rs.getInt("progress"));
                listofdetails.add(mp);
            }
            System.out.println(listofdetails);
        } catch (SQLException e) {
            System.out.println(e);
        }
        return listofdetails;
    }
    @GetMapping("/update")
    public String updatePage(Model model) {
        List<Map<String, Object>> data = fetchTasks(teamid);
        model.addAttribute("taskdetail", data);
        return "update";
    }

    @ModelAttribute("taskdetail")
    public List<Map<String, Object>> fetchTasks(String teamid) {
        List<Map<String, Object>> listofdetails = new ArrayList<>();
        try  {
            Connection connection = DriverManager.getConnection(jdbcurl, "root", "poovarasan@13");
            String sql = "SELECT * FROM " + teamid;
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> taskMap = new HashMap<>();
                    taskMap.put("UserName", rs.getString("username"));
                    taskMap.put("TeamNo", rs.getString("teamno"));
                    listofdetails.add(taskMap);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listofdetails;
    }

    @PostMapping("/updatetask")
    public String update( @RequestParam("sender") String sender, @RequestParam("task") String task,@RequestParam("enddate") Date enddate,@RequestParam("receiver") String receiver,@RequestParam("progress") int progress) {
        System.out.println(teamid);

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(jdbcurl, "root", "poovarasan@13");

            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, null, teamid + "task", null);

            if (!tables.next()) {

                String sql = "create table  " + teamid + "task (sender varchar(20), task varchar(100), enddate date, receiver varchar(20),progress int)";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.execute();
            }


            String insert = "insert into " + teamid + "task values(?,?,?,?,?)";
            PreparedStatement statement1 = connection.prepareStatement(insert);
            statement1.setString(1, sender);
            statement1.setString(2, task);
            statement1.setDate(3, enddate);
            statement1.setString(4, receiver);
            statement1.setInt(5, progress);

            statement1.executeUpdate();

        } catch (Exception e) {
            System.out.println(e);
        }
        return "justview";
    }
@GetMapping("/about")
    public String about()
{
    return "about";
}
    @GetMapping("/contact")
    public String contact()
    {
        return "contact";
    }
    @GetMapping("/mainpage")
    public String mainpage()
    {
        return "mainpage";
    }

//    @PostMapping("/viewtask")
//    public String view(Model model, @RequestParam("name") String name) {
//        System.out.println("Inside view task");
//        Connection connection = null;
//        List<Map<String, Object>> task = new ArrayList<>();
//        try {
//            connection = DriverManager.getConnection(jdbcurl, "root", "poovarasan");
//            String sql = "select * from task where reciever=?";
//            PreparedStatement statement = connection.prepareStatement(sql);
//            statement.setString(1, name); // Use 1 instead of 4 for setting parameters
//            ResultSet rs = statement.executeQuery();
//
//            while (rs.next()) {
//                Map<String, Object> mp = new HashMap<>();
//                mp.put("sender", rs.getString("sender"));
//                mp.put("task", rs.getString("task"));
//                mp.put("end_date", rs.getString("end_date"));
//                mp.put("reciever", rs.getString("reciever"));
//                mp.put("progress", rs.getString("progress"));
//                task.add(mp);
//            }
//        } catch (SQLException e) {
//            System.out.println(e);
//        } finally {
//            try {
//                if (connection != null) {
//                    connection.close();
//                }
//            } catch (SQLException e) {
//                System.out.println(e);
//            }
//        }
//       /// model.addText("dataList",task); // Change addText to addAttribute
//        return "view";
//    }


//@PostMapping("/delete")
//    public String delete()
//{
//    Connection connection=null;
//    try{
//
//    }
//    return "delete";
//}


    @PostMapping("/delete")
    public String  delete1(@RequestParam("selectedRow") String selectrow,Model model ){
        // to get the task fromm the table
//        {username=abc,userid=def}
//        {username,abc
        String task="";
        String teamtask[]=selectrow.split(",");
        for(String keyvalue:teamtask){
            System.out.println(keyvalue);
            //keyvlue="sender=name"
            //keyvalue="task=do this"
            String keyvaluearr[]=keyvalue.trim().split("=");
            //{username,abc    but here the task is nnot in begin so we not want to .substring
            if(keyvaluearr[0].equals("Task")){
                task=keyvaluearr[1];
            }
        }


        Connection connection=null;
        try{
            connection=DriverManager.getConnection(jdbcurl,"root","poovarasan@13");
            String sql=" delete from "+teamid+"task where task='"+task+"'";
            PreparedStatement statement=connection.prepareStatement(sql);
            statement.executeUpdate();

            System.out.println("deleted");


        }
        catch (Exception e){
            System.out.println(e);
        }
        return "/justview";


    }







}
