package com.test.datasource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

public class ConnectionPool implements DataSource {


    public static ConcurrentLinkedQueue<Connection> Connections = new ConcurrentLinkedQueue();

    static {
       InputStream inputStream =  ConnectionPool.class.getClassLoader().getResourceAsStream("db.properties");
       Properties properties = new Properties();
        try {
            properties.load(inputStream);
            String driver = properties.getProperty("driver");
            String url = properties.getProperty("url");
            String username = properties.getProperty("username");
            String password = properties.getProperty("password");
            //链接数量
            Integer size = Integer.parseInt(properties.getProperty("jdbcPoolInitSize"));

            //加载驱动
            Class.forName(driver);
            for(int i =0;i<size;i++) {
                Connection conn = DriverManager.getConnection(url, username, password);
                Connections.add(conn);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println(inputStream == null);
    }



    @Override
    public Connection getConnection()  {
        if(Connections.size() > 0){
            Connection connection = Connections.poll();
            System.out.println("获取链接"+ConnectionPool.Connections.size());
            //创建代理
           return (Connection) Proxy.newProxyInstance(connection.getClass().getClassLoader(), connection.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if(method.getName() .equals("close")){
                        Connections.add(connection);
                        System.out.println("链接关闭"+ConnectionPool.Connections.size());
                    }else {
                       return  method.invoke(connection, args);
                    }
                    return  null;
                }
            });

        }
        return null;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {

    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {

    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    public static void main(String[] args) {
        ConnectionPool connections =  new ConnectionPool();
        Connection connection = connections.getConnection();
        try {
            Statement st  = connection.createStatement();
            String sql = "select * from yb_user ";
            ResultSet rs=st.executeQuery(sql);
            System.out.println(rs.toString());
            while(rs.next()){                                                      //rs.next()   表示如果结果集rs还有下一条记录，那么返回true；否则，返回false
                int id = rs.getInt("id");
                String name = rs.getString(2);
                String sex = rs.getString(3);
                System.out.println(id+"--->"+name+"--------"+sex);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
