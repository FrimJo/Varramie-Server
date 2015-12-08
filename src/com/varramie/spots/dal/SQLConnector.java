package com.varramie.spots.dal;

import java.sql.* ;  // for standard JDBC programs
import java.util.concurrent.LinkedBlockingQueue;

import com.varramie.spots.server.*;
import com.varramie.spots.keys.*;


/*java.util.Date javaDate = new java.util.Date(); long javaTime = javaDate.getTime(); */

public class SQLConnector {
		
	private final LinkedBlockingQueue<String> queryQ = new LinkedBlockingQueue<>();
	private final Thread writerThread;
	private boolean stop = false;
	private Connection sqlConnection;
	
	public SQLConnector(){
		
		try{
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		} catch(ClassNotFoundException ex) {
			Server.INSTANCE.println("Error: unable to load driver class!");
			System.exit(1);
		}
		
		
		writerThread = new Thread(){
			
			@Override
			public void run(){
				
				try (Connection conn = DriverManager.getConnection(Keys.SQL_CONNECTION_STRING, Keys.SQL_USERNAME, Keys.SQL_PASSWORD)) {
					conn.setAutoCommit(false);
					Server.INSTANCE.println("SQL: connection established.");
					
					while(!stop){
						String query = queryQ.take();
					
						try(Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)){
							
							stmt.executeUpdate(query);
						
							conn.commit( );	
						}catch(SQLException e){
							Server.INSTANCE.println("Error: unable to create statement.");
							conn.rollback();
						}
					}

				} catch (SQLException e1) {
					Server.INSTANCE.println("Error: unable to connect to database server.");
				} catch (InterruptedException e2) {
					Server.INSTANCE.println("SQL: writer thread interrupted.");
				}
			}
		};
		writerThread.start();
		
		
		// Initialize the connection of the SQL server 
		
	}
	
	public void addEntry(final float x, final float y, final float pressure, final byte action, final String id, final float vel_x, final float vel_y){
		try {
			queryQ.put(	"INSERT INTO dbo.Entries (log_pos_x, log_pos_y, log_pressure, log_action, log_connection_id, log_velocity_x, log_velocity_y)" +
						" VALUES ("+x+", "+y+", "+pressure+", "+action+", '"+id+"', "+vel_x+", "+vel_y+")");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void addEntry(final float x, final float y, final String collision_idA, final String collision_idB, final byte action){
		try {
			queryQ.put(	"INSERT INTO dbo.Collision (col_pos_x, col_pos_y, col_connection_idA, col_connection_idB, col_action)" +
						" VALUES ("+x+", "+y+", '"+collision_idA+"', '"+collision_idB+"', "+action+")");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	
	public void openConnection(){
		
		try {
			sqlConnection= DriverManager.getConnection(Keys.SQL_CONNECTION_STRING, Keys.SQL_USERNAME, Keys.SQL_PASSWORD);
			sqlConnection.setAutoCommit(false);
		} catch (SQLException e) {
			Server.INSTANCE.println("Error: unable to connect to database server.");
		}finally{
			if(sqlConnection != null)
				try {
					sqlConnection.close();
				} catch (SQLException e) {

				}
		}
		
	}
	
	public void close(){
		stop = true;
		writerThread.interrupt();
	}
}
