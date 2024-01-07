
package lk.captain.Controller;

        import com.jfoenix.controls.JFXButton;
        import com.jfoenix.controls.JFXComboBox;
        import javafx.collections.FXCollections;
        import javafx.collections.ObservableList;
        import javafx.event.ActionEvent;
        import javafx.fxml.FXML;
        import javafx.fxml.FXMLLoader;
        import javafx.scene.Cursor;
        import javafx.scene.Parent;
        import javafx.scene.Scene;
        import javafx.scene.control.*;
        import javafx.scene.control.cell.PropertyValueFactory;
        import javafx.stage.Stage;
        import lk.captain.db.DbConnection;
        import lk.captain.dto.*;
        import lk.captain.dto.tm.OrderCartTM;
        import lk.captain.model.AddCustomerModel;
        import lk.captain.model.StoreDetailsModel;
        import lk.captain.model.TeaSellsModel;
        import lk.captain.model.TeaTypeModel;
        import net.sf.jasperreports.engine.*;
        import net.sf.jasperreports.engine.design.JRDesignQuery;
        import net.sf.jasperreports.engine.design.JasperDesign;
        import net.sf.jasperreports.engine.xml.JRXmlLoader;
        import net.sf.jasperreports.view.JasperViewer;

        import java.io.IOException;
        import java.io.InputStream;
        import java.sql.SQLException;
        import java.time.LocalDate;
        import java.time.LocalTime;
        import java.time.format.DateTimeFormatter;
        import java.util.*;
        import java.util.regex.Pattern;

        import static net.sf.jasperreports.engine.JasperCompileManager.compileReport;

public class TeaSellsController {

    @FXML
    private JFXButton addCartBtn;

    @FXML
    private JFXComboBox<String> cmbCusId;

    @FXML
    private JFXComboBox<String> cmbTeaId;

    @FXML
    private Label lblCusName;

    @FXML
    private Label lblDescrip;

    @FXML
    private Label lblOrderDate;

    @FXML
    private Label lblOrderId;

    @FXML
    private Label lblQoH;

    @FXML
    private TableColumn<?, ?> colCodeTea;

    @FXML
    private Label lblNettotal;

    @FXML
    private Label lblUnitPrice;

    @FXML
    private TableColumn<?, ?> colAction;

    @FXML
    private TableColumn<?, ?> colQuantity;

    @FXML
    private TableColumn<?, ?> colTeaName;

    @FXML
    private TableColumn<?, ?> colTotal;

    @FXML
    private TableColumn<?, ?> colUnitPrice;

    @FXML
    private JFXButton placeIdBtn;

    @FXML
    private JFXButton printBtn;

    @FXML
    private TableView<OrderCartTM> tblPlaceOrder;

    @FXML
    private TextField txtQuan;
    @FXML
    private Label lblTime;


    TeaTypeModel teaTypeModel = new TeaTypeModel();
    AddCustomerModel addCustomerModel = new AddCustomerModel();
    StoreDetailsModel storeDetailsModel = new StoreDetailsModel();
    TeaSellsModel teaSellsModel = new TeaSellsModel();
    private final ObservableList<OrderCartTM> obList = FXCollections.observableArrayList();

    public void initialize() {
        load();
        lblOrderDate.setText(String.valueOf(LocalDate.now()));
        generateNextOrderId();
        setCellValueFactory();
        tblPlaceOrder.refresh();
    }

    private void generateNextOrderId() {
        try {
            String orderId = teaSellsModel.generateNextOrderId();
            lblOrderId.setText(orderId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void btnPlaceAction(ActionEvent event) throws JRException {

        String time = lblTime.getText();
        String orderId = lblOrderId.getText();
        String cusId = cmbCusId.getValue();
        String date = lblOrderDate.getText();
        String teaTypeName = lblDescrip.getText();

        List<OrderCartTM> tmList = new ArrayList<>();

        for (OrderCartTM cartTm : obList) {
            tmList.add(cartTm);
        }

        var teasellsDto = new TeaSellsDTO(
                orderId,
                cusId,
                date,
                teaTypeName,
                time,
                tmList

        );

        try {
            boolean isSuccess = teaSellsModel.placeOrder(teasellsDto);
            if (isSuccess) {
                new Alert(Alert.AlertType.CONFIRMATION, " order completed!").show();
                report();
                generateNextOrderId();
                setCellValueFactory();
                tblPlaceOrder.refresh();

            }
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
        }

    }

    @FXML
    void btnaddCartAction(ActionEvent event) {
       double qth = Double.parseDouble(lblQoH.getText());
       double qty = Double.parseDouble(txtQuan.getText());
       if (qth<qty){
           new Alert(Alert.AlertType.ERROR,"OUTOFSTOCK !").show();
           txtQuan.setStyle("-fx-border-color: red");
       }else if (qth>=qty){
           boolean isValid = Valid();
           if (isValid == true) {
               String teaTypeId = cmbTeaId.getValue();
               String teaTypeName = lblDescrip.getText();
               int quantity = Integer.parseInt(txtQuan.getText());
               double unitPrice = Double.parseDouble(lblUnitPrice.getText());
               double total = quantity * unitPrice;
               Button btn = new Button("Remove");
               btn.setCursor(Cursor.HAND);

               btn.setOnAction((e) -> {
                   ButtonType yes = new ButtonType("yes", ButtonBar.ButtonData.OK_DONE);
                   ButtonType no = new ButtonType("no", ButtonBar.ButtonData.CANCEL_CLOSE);

                   Optional<ButtonType> type = new Alert(Alert.AlertType.INFORMATION, "Are you sure to remove?", yes, no).showAndWait();

                   if (type.orElse(no) == yes) {
                       int index = tblPlaceOrder.getSelectionModel().getSelectedIndex();
                       obList.remove(index);
                       tblPlaceOrder.refresh();

                       calculateNetTotal();
                   }
               });
               for (int i = 0; i < tblPlaceOrder.getItems().size(); i++) {
                   if (teaTypeId.equals(colCodeTea.getCellData(i))) {
                       quantity += (double) colQuantity.getCellData(i);
                       total = quantity * unitPrice;

                       obList.get(i).setQuantity(quantity);
                       obList.get(i).setTotal(total);

                       tblPlaceOrder.refresh();
                       calculateNetTotal();
                       return;
                   }
               }

               obList.add(new OrderCartTM(
                       teaTypeName,
                       teaTypeId,
                       quantity,
                       unitPrice,
                       total,
                       btn
               ));

               tblPlaceOrder.setItems(obList);
               calculateNetTotal();
               //  txtQuan.clear();
           }
       }
    }

    private void calculateNetTotal() {
        double total = 0;
        for (int i = 0; i < tblPlaceOrder.getItems().size(); i++) {
            total += (double) colTotal.getCellData(i);
        }
        lblNettotal.setText(String.valueOf(total));

    }

    private void setCellValueFactory() {
        colTeaName.setCellValueFactory(new PropertyValueFactory<>("teaTypeName"));
        colCodeTea.setCellValueFactory(new PropertyValueFactory<>("teaTypeId"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colUnitPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("btn"));
    }

    @FXML
    void cmbCusIdAction(ActionEvent event) throws SQLException {
        String cusId = cmbCusId.getValue();
        AddCustomerDTO dto = addCustomerModel.searchCusId(cusId);
        lblCusName.setText(dto.getCusName());
        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedTime = currentTime.format(formatter);
        lblTime.setText(formattedTime);

    }

    @FXML
    void cmbTeaIdAction(ActionEvent event) throws SQLException {
        String teaid = cmbTeaId.getValue();
        TeaTypeDTO dto = teaTypeModel.serchOnTeaType(teaid);
        lblDescrip.setText(dto.getTeaTypeName());
        lblUnitPrice.setText(String.valueOf(dto.getTeaPerPrice()));
        StoreDetailsDTO storeTeaType = storeDetailsModel.serchOnTeaType(teaid);
        lblQoH.setText(String.valueOf(storeTeaType.getQuantity()));

    }

    private void load() {
        ObservableList<String> obCusId = FXCollections.observableArrayList();
        ObservableList<String> obTeaTypeId = FXCollections.observableArrayList();
        try {
            List<AddCustomerDTO> suppList = addCustomerModel.getAllCus();
            List<TeaTypeDTO> ColecList = teaTypeModel.loadAllTeaTypes();

            for (AddCustomerDTO dto : suppList) {
                obCusId.add(dto.getCusId());
            }

            for (TeaTypeDTO dto : ColecList) {
                obTeaTypeId.add(dto.getTeaTypeId());
            }
            cmbCusId.setItems(obCusId);
            cmbTeaId.setItems(obTeaTypeId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @FXML
    void btnAddCusAction(ActionEvent event) throws IOException {
        Parent rootNode = FXMLLoader.load(this.getClass().getResource("/view/addCustomer_form.fxml"));
        Scene scene = new Scene(rootNode);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Customer Page");
        load();
        stage.show();
    }


    public void report(){
        String cusId = cmbCusId.getValue();
        String order = lblOrderId.getText();


        try {
            InputStream resourceAsStream = getClass().getResourceAsStream("/report/OrdersTeaCategoryReport.jrxml");
            JasperDesign load = JRXmlLoader.load(resourceAsStream);

            JRDesignQuery jrDesignQuery = new JRDesignQuery();
            jrDesignQuery.setText("SELECT o.unitPrice,\n" +
                    "                    o.quantity,\n" +
                    "                      o.total,\n" +
                    "                    o.orderId,\n" +
                    "                    t.teaTypeName,\n" +
                    "                     (SELECT cusName FROM customer WHERE cusId ='"+cusId+"')AS CUSNAME,\n" +
                    "                    (SELECT SUM(total) FROM orders WHERE cusId = '"+cusId+"' AND orderId='"+order+"') AS total_orders\n" +
                    "                    FROM orders o\n" +
                    "                           JOIN tea_Types t ON o.teaTypeId = t.teaTypeId\n" +
                    "                    WHERE o.cusId ='"+cusId+"' AND o.orderId = '"+order+"'");
            load.setQuery(jrDesignQuery);

            JasperReport compiledReport = JasperCompileManager.compileReport(load);
            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    compiledReport,
                    null,
                    DbConnection.getInstance().getConnection()
            );
            JasperViewer.viewReport(jasperPrint,false);
        }catch (JRException | SQLException e){
            e.printStackTrace();
        }

    }
    private boolean Valid() {

        String quan = txtQuan.getText();
        boolean isQuan =  Pattern.matches("[0-9]{0,}", quan);

        if (!isQuan) {
            new Alert(Alert.AlertType.ERROR, "Invalid Quantity").show();
            return false;
        }
        return true;

    }
}
