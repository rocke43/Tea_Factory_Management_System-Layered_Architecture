package lk.captain.dao.custom;

import lk.captain.dao.SQLUtil;
import lk.captain.entity.Wood;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class WoodDAOImpl implements WoodDAO{
    @Override
    public boolean delete(String id) throws SQLException, ClassNotFoundException {
        return SQLUtil.execute("UPDATE matirials SET wCategory = NULL, wWeight = NULL WHERE barrelId = ? ",id);
    }

    @Override
    public boolean save(Wood dto) throws SQLException, ClassNotFoundException {
        return SQLUtil.execute("INSERT INTO matirials(barrelId,wCategory,wWeight) VALUES (?,?,?)"
                ,dto.getBarrelId(),dto.getWCategory(),dto.getWWeight()
        );
    }

    @Override
    public boolean update(Wood dto) throws SQLException, ClassNotFoundException {
       return SQLUtil.execute("UPDATE matirials SET wCategory = ?, wWeight = ? WHERE barrelId= ?",
                dto.getWCategory(),
                dto.getWWeight(),
                dto.getBarrelId()
        );
    }

    @Override
    public Wood search(String id) throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute("select matirials.barrelId,wCategory,wWeight FROM matirials where barrelId= ? ");
        if (rst.next()) {
            return new Wood(
                    rst.getString(1),
                    rst.getString(2),
                    rst.getDouble(3)
            );
        }
        return null;

    }

    @Override
    public String generateId() throws SQLException, ClassNotFoundException {
        return null;
    }

    @Override
    public ArrayList<Wood> getAll() throws SQLException, ClassNotFoundException {
        ResultSet rst = SQLUtil.execute("SELECT matirials.barrelId, wCategory, wWeight\n" +
                "FROM matirials\n" +
                "WHERE  wCategory IS NOT NULL\n" +
                "  AND wWeight IS NOT NULL");
        ArrayList<Wood> allWood = new ArrayList<>();

while (rst.next()) {
           allWood.add(new Wood(
                    rst.getString(1),
                    rst.getString(2),
                    rst.getDouble(3)
            ));
        }
        return allWood;
    }

    @Override
    public int searchCount() throws SQLException, ClassNotFoundException {
        return 0;
    }
}