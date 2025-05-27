package CashFlowy.persistence.repository;

//import GFPrototype.src.main.java.persistence.model.Transazione;
import CashFlowy.persistence.model.Transaction;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TransactionRepository implements Repository<Transaction, Long> {
    private final HikariDataSource dataSource;

    public TransactionRepository(HikariDataSource dataSource) {
        this.dataSource = dataSource;
        checkTable();
    }

    private void checkTable() {
        String sql = "SELECT * FROM transactions LIMIT 1";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet rs = statement.executeQuery();
        } catch (SQLException e) {
            initTable();
        }
    }

    private void initTable() {
        String sql = "DROP TABLE IF EXISTS transactions;" +
                "CREATE TABLE transactions " +
                "(id SERIAL, " +
                "categoria VARCHAR(50) DEFAULT NULL, " +
                "descrizione VARCHAR(50) DEFAULT NULL, " +
                "importo DOUBLE PRECISION DEFAULT NULL, " +
                "data DATE DEFAULT NULL, " +
                "tipo VARCHAR(50) DEFAULT NULL, " +
                "PRIMARY KEY (id))";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Optional<Transaction> findById(Long Id) {
        String sql = "SELECT * FROM transactions WHERE id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, Id);
            ResultSet rs = statement.executeQuery();

            if (!rs.next()) {
                return Optional.empty();
            }

            return Optional.of(new Transaction(rs.getLong("id"),rs.getString("categoria"), rs.getString("descrizione"),rs.getDouble("importo"),  rs.getDate("data").toLocalDate(), rs.getString("tipo")));
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Iterable<Transaction> findAll() {
        String sql = "SELECT * FROM transactions";
        List<Transaction> TransactionList = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                TransactionList.add(new Transaction(rs.getLong("id"),rs.getString("categoria"), rs.getString("descrizione"),rs.getDouble("importo"),  rs.getDate("data").toLocalDate(), rs.getString("tipo")));
            }
            return TransactionList;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    @Override
    public Transaction save(Transaction entity) {
        if (Objects.isNull(entity.getId())) {
            return insert(entity);
        }

        Optional<Transaction> Transaction = findById(entity.getId());
        if (Transaction.isEmpty()) {
            return insert(entity);
        } else {
            return update(entity);
        }
    }

    private Transaction insert(Transaction entity) {
        String sql = "INSERT INTO transactions (categoria, descrizione, importo, data, tipo) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, entity.getCategoria());
            statement.setString(2, entity.getDescrizione());
            statement.setDouble(3, entity.getImporto());
            statement.setDate(4, Date.valueOf(entity.getData()));
            statement.setString(5, entity.getTipo());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                entity.setId(keys.getLong(1));
                return entity;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private Transaction update(Transaction entity) {
        String sql = "UPDATE transactions SET categoria=?, descrizione=?, importo=?, data=?, tipo=? WHERE id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entity.getCategoria());
            statement.setString(2, entity.getDescrizione());
            statement.setDouble(3, entity.getImporto());
            statement.setDate(4, Date.valueOf(entity.getData()));
            statement.setString(5, entity.getTipo());
            statement.setLong(6, entity.getId());
            statement.executeUpdate();
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void delete(Transaction entity) {
        deleteById(entity.getId());
    }

    @Override
    public void deleteById(Long Id) {
        String sql = "DELETE FROM transactions WHERE id=?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, Id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM transactions";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
