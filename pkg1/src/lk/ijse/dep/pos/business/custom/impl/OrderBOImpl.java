package lk.ijse.dep.pos.business.custom.impl;

import lk.ijse.dep.pos.business.custom.OrderBO;
import lk.ijse.dep.pos.dao.custom.*;

import lk.ijse.dep.pos.dto.OrderDTO;
import lk.ijse.dep.pos.dto.OrderDTO2;
import lk.ijse.dep.pos.dto.OrderDetailDTO;
import lk.ijse.dep.pos.entity.*;
import lk.ijse.dep.pos.hibernate.HibernateUtil;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class OrderBOImpl implements OrderBO {

    @Autowired
    private OrderDAO orderDAO;
    @Autowired
    private OrderDetailDAO orderDetailDAO;
    @Autowired
    private ItemDAO itemDAO;
    @Autowired
    private QueryDAO queryDAO;
    @Autowired
    private CustomerDAO customerDAO;

    @Override
    public int getLastOrderId() throws Exception {
        int orderId;
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            orderDAO.setSession(session);
            session.beginTransaction();
            orderId = orderDAO.getLastOrderId();
            session.getTransaction().commit();
        }
        return orderId;
    }

    @Override
    public void placeOrder(OrderDTO order) throws Exception {

        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            orderDAO.setSession(session);
            itemDAO.setSession(session);
            customerDAO.setSession(session);
            orderDetailDAO.setSession(session);
            session.beginTransaction();

            int oId = order.getId();
            orderDAO.save(new Orders(oId,new java.sql.Date(new Date().getTime()),session.load(Customer.class,order.getCustomerId())));

            for (OrderDetailDTO orderDetail : order.getOrderDetails()) {
                orderDetailDAO.save(new OrderDetail(oId,orderDetail.getCode(),orderDetail.getQty(),orderDetail.getUnitPrice()));

                Item item = itemDAO.find(orderDetail.getCode());
                item.setQtyOnHand(item.getQtyOnHand() - orderDetail.getQty());
                itemDAO.update(item);

            }


            session.getTransaction().commit();
        }
    }

    @Override
    public List<OrderDTO2> getOrderInfo() throws Exception {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            queryDAO.setSession(session);
            session.beginTransaction();

            List<CustomEntity> ordersInfo = queryDAO.getOrdersInfo();
            List<OrderDTO2> dtos = new ArrayList<>();
            for (CustomEntity info : ordersInfo) {
                dtos.add(new OrderDTO2(info.getOrderId(),
                        info.getOrderDate(),info.getCustomerId(),info.getCustomerName(),info.getOrderTotal()));
            }
            session.getTransaction().commit();
            return dtos;
        }
    }

    @Override
    public List<OrderDTO2> getSearchInfo(String searchText) throws Exception {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            queryDAO.setSession(session);
            session.beginTransaction();

            List<CustomEntity> ordersInfo = queryDAO.getSearchInfo(searchText);
            List<OrderDTO2> dtos = new ArrayList<>();
            for (CustomEntity info : ordersInfo) {
                dtos.add(new OrderDTO2(info.getOrderId(),
                        info.getOrderDate(),info.getCustomerId(),info.getCustomerName(),info.getOrderTotal()));
            }
            session.getTransaction().commit();
            return dtos;
        }
    }
}
