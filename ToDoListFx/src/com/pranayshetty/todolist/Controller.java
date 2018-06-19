package com.pranayshetty.todolist;

import com.pranayshetty.todolist.datamodel.ToDoData;
import com.pranayshetty.todolist.datamodel.ToDoItem;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;


public class Controller {
    private List<ToDoItem> toDoItems;
    @FXML
    private ListView<ToDoItem> todoListView;
    @FXML private TextArea itemInTextArea;
    @FXML private Label deadlineLabel;
    @FXML private BorderPane mainBorderPane;
    @FXML private ContextMenu listContextMenu;
    @FXML private ToggleButton filterToggleButton;
    private FilteredList<ToDoItem> filteredList;

    private Predicate<ToDoItem> wantAllItems;
    private Predicate<ToDoItem> wantTodaysItems;


    public void initialize(){

        listContextMenu=new ContextMenu();
        MenuItem deleteMenuItem=new MenuItem("Delete ");
        deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ToDoItem item= todoListView.getSelectionModel().getSelectedItem();
                deleteItem(item);
            }
        });

        listContextMenu.getItems().addAll(deleteMenuItem);
        todoListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue!= null){
                ToDoItem item=todoListView.getSelectionModel().getSelectedItem();
                itemInTextArea.setText(item.getDetails());
                deadlineLabel.setText(item.getDeadline().toString());
            }
        });

        wantAllItems=new Predicate<ToDoItem>() {
            @Override
            public boolean test(ToDoItem item) {
                return true;
            }
        };

        wantTodaysItems=new Predicate<ToDoItem>() {
            @Override
            public boolean test(ToDoItem item) {
                return item.getDeadline().equals(LocalDate.now());
            }
        };

        filteredList=new FilteredList<ToDoItem>(ToDoData.getInstance().getToDoItems(), wantAllItems);


        SortedList<ToDoItem> sortedList=new SortedList<ToDoItem>(filteredList, new Comparator<ToDoItem>() {
            @Override
            public int compare(ToDoItem o1, ToDoItem o2) {
                return o1.getDeadline().compareTo(o2.getDeadline());
            }
        });


//        todoListView.setItems(ToDoData.getInstance().getToDoItems());
        todoListView.setItems(sortedList);

        //todoListView.getItems().setAll(ToDoData.getInstance().getToDoItems());
        todoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        todoListView.getSelectionModel().selectFirst();
        todoListView.setCellFactory(new Callback<ListView<ToDoItem>, ListCell<ToDoItem>>() {
            @Override
            public ListCell<ToDoItem> call(ListView<ToDoItem> param) {
                ListCell<ToDoItem> cell=new ListCell<ToDoItem>(){
                    @Override
                    protected void updateItem(ToDoItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if(empty){
                            setText(null);
                        }else{
                            setText(item.getShortDescription());
                            if(item.getDeadline().isBefore(LocalDate.now().plusDays(1))){
                                setTextFill(Color.AZURE);
                            }
                            if(item.getDeadline().equals(LocalDate.now())){
                                setTextFill(Color.RED);
                            }else if(item.getDeadline().equals(LocalDate.now().plusDays(1))){
                                setTextFill(Color.YELLOW);
                            }else{
                                setTextFill(Color.GREEN);
                            }
                        }
                    }
                };
                cell.emptyProperty().addListener(
                        (obs , wasEmpty, isNowEmpty)-> {
                            if(isNowEmpty){
                                cell.setContextMenu(null);
                            }
                            else{
                                cell.setContextMenu(listContextMenu);
                            }
                        });
                return cell;


            }
        });

    }

    @FXML
    public void handleKeyPressed(KeyEvent keyEvent){
        ToDoItem selectedItem=todoListView.getSelectionModel().getSelectedItem();
        if(selectedItem!=null){
            if(keyEvent.getCode().equals(KeyCode.DELETE)){
                deleteItem(selectedItem);
            }
        }

    }

    @FXML
    public void showNewItemDialog(){
        Dialog<ButtonType> dialog =new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Add new Items");
        dialog.setHeaderText("Use this dialog to create a new Item");
        FXMLLoader fxmlLoader=new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("ToDoItemDialog.fxml"));
        try{
            dialog.getDialogPane().setContent(fxmlLoader.load());


        }catch (IOException e){
            System.out.println("Couldnt load dialog");
            e.printStackTrace();
            return;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        Optional<ButtonType> result=dialog.showAndWait();
        if(result.isPresent() && result.get()==ButtonType.OK){
            DialogController controller=fxmlLoader.getController();
            ToDoItem newItem=controller.processResults();
            //todoListView.getItems().setAll(ToDoData.getInstance().getToDoItems());
            todoListView.getSelectionModel().select(newItem);
        }

    }

    /*@FXML
    public void handleClickListView(){
        ToDoItem item = todoListView.getSelectionModel().getSelectedItem();
        itemInTextArea.setText(item.getDetails());
        deadlineLabel.setText(item.getDeadline().toString());
    }*/
    public void deleteItem(ToDoItem item){
        Alert alert=new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Item");
        alert.setHeaderText("Delete your item");
        alert.setContentText("Are you sure you want to delete this item\nDetails: "+item.getShortDescription());
        Optional<ButtonType> result=alert.showAndWait();
        if(result.isPresent() && result.get() ==ButtonType.OK){
            ToDoData.getInstance().deleteToDoItem(item);
        }
    }

    @FXML  public void handleFilterButton(){
        ToDoItem selectedItem=todoListView.getSelectionModel().getSelectedItem();

        if(filterToggleButton.isSelected()){
            filteredList.setPredicate(wantTodaysItems);
            if(filteredList.isEmpty()){
                itemInTextArea.clear();
                deadlineLabel.setText("");
            }else if(filteredList.contains(selectedItem)){
                todoListView.getSelectionModel().select(selectedItem);
            }
            else{
                todoListView.getSelectionModel().selectFirst();
            }
        }else{
            filteredList.setPredicate(wantAllItems);
            todoListView.getSelectionModel().select(selectedItem);
        }
    }


    @FXML public  void handleExit(){
        Platform.exit();
    }


}
