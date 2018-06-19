package com.pranayshetty.todolist;

import com.pranayshetty.todolist.datamodel.ToDoData;
import com.pranayshetty.todolist.datamodel.ToDoItem;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;


public class DialogController {
    @FXML private TextField shortDescriptionField;
    @FXML private TextArea detailsArea;
    @FXML private DatePicker deadlinePicker;

    @FXML public ToDoItem processResults(){
        String shortDescription =shortDescriptionField.getText().trim();
        String details =detailsArea.getText().trim();
        LocalDate deadlineValue=deadlinePicker.getValue();

        ToDoItem newItem=new ToDoItem(shortDescription,details,deadlineValue);
        ToDoData.getInstance().addToDoItem(newItem);
        return newItem;
    }
}
