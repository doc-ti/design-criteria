package edu.doc_ti.designcriteria.common.objects.test;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchStatement;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.DefaultBatchType;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.metadata.schema.ColumnMetadata;
import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata;
import com.datastax.oss.driver.api.core.uuid.Uuids;

public class CassandraBatchInserter {

    private final CqlSession session;
    private final String keyspace;

    public CassandraBatchInserter(String contactPoint, int port, String datacenter, String keyspace) {
        this.keyspace = keyspace;
        this.session = CqlSession.builder()
                .addContactPoint(new InetSocketAddress(contactPoint, port))
                .withLocalDatacenter(datacenter)
                .withKeyspace(keyspace)
                .build();
    }

    public List<String> getTableColumns(String tableName) {
        Optional<TableMetadata> tableMetadata = session.getMetadata()
                .getKeyspace(keyspace)
                .flatMap(ks -> ks.getTable(tableName));

//        if (tableMetadata.isEmpty()) {
//            throw new IllegalArgumentException("Tabla no encontrada: " + tableName);
//        }

        List<String> columns = new ArrayList<>();
        for (ColumnMetadata col : tableMetadata.get().getColumns().values()) {
            columns.add(col.getName().asInternal());
            System.out.println( col.getName().asInternal() ) ;
        }
        
        
        return columns;
    }

    public void insertBatch(String tableName, List<Map<String, Object>> records) {
        List<String> columns = getTableColumns(tableName);
        Set<String> columnSetLower = new HashSet<>();
        for (String col : columns) {
            columnSetLower.add(col.toLowerCase());
        }

        BatchStatementBuilder batch = BatchStatement.builder(DefaultBatchType.LOGGED);

        for (Map<String, Object> record : records) {
            Map<String, Object> matchedData = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : record.entrySet()) {
                String key = entry.getKey().toLowerCase();
                if (columnSetLower.contains(key)) {
                    for (String col : columns) {
                        if (col.equalsIgnoreCase(entry.getKey())) {
                            matchedData.put(col, entry.getValue());
                            break;
                        }
                    }
                }
            }

            if (!matchedData.isEmpty()) {
                StringBuilder query = new StringBuilder("INSERT INTO ")
                        .append(tableName).append(" (")
                        .append(String.join(", ", matchedData.keySet()))
                        .append(") VALUES (")
                        .append(String.join(", ", Collections.nCopies(matchedData.size(), "?")))
                        .append(")");

                PreparedStatement ps = session.prepare(query.toString());
                BoundStatement bs = ps.bind(matchedData.values().toArray());
                batch.addStatement(bs);
            }
        }

        session.execute(batch.build());
    }

    public void close() {
        session.close();
    }

    // Ejemplo de uso
    public static void main(String[] args) {
    	
        CassandraBatchInserter inserter = new CassandraBatchInserter("10.0.0.3", 9042, "dc1", "my_keyspace");

        ArrayList<Map<String, Object>> ll = new ArrayList<Map<String, Object>>() ;
        
        
//        Map<String, Object> row1 = new HashMap<>();
//        row1.put("uuid", Uuids.timeBased() ) ;
//        row1.put("id", 1);
//        row1.put("nombre", "Juan");
//        ll.add(row1) ;
//
//        Map<String, Object> row2 = new HashMap<>();
//        row2.put("uuid", Uuids.timeBased() ) ;
//        row2.put("id", 2);
//        row2.put("nombre", "Ana");
//        ll.add(row2) ;

        for ( int n = 1 ; n<100; n++) {
            Map<String, Object> row = new HashMap<>();
            row.put("uuid", Uuids.timeBased() ) ;
            row.put("id", n);
            row.put("nombre", "Ana");
            ll.add(row) ;
        }

        inserter.insertBatch("usuarios", ll);
        inserter.close();
    }
}
