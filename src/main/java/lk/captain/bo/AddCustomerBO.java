package lk.captain.bo;

import lk.captain.dto.AddCustomerDTO;
import lk.captain.entity.AddCustomer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public interface AddCustomerBO extends SuperBO{
     boolean addCustomer(AddCustomerDTO addCustomerDTO) throws SQLException, ClassNotFoundException;
     String generateCusId() throws SQLException, ClassNotFoundException;
     boolean deleteCus(String cusIds) throws SQLException, ClassNotFoundException;
     boolean updateCustomer(AddCustomerDTO dto) throws SQLException, ClassNotFoundException;
     AddCustomerDTO searchCusId(String id) throws SQLException, ClassNotFoundException;
     ArrayList<AddCustomerDTO> getAllCus() throws SQLException, ClassNotFoundException;
}
