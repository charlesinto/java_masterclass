import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class WarehouseMgt {
    public static void main(String[] args) {
        OrderService orderService = new OrderService(0, 3);
        Thread producerThread = new Thread(new OrderProducer(orderService));
        Thread consumerThread = new Thread(new OrderConsumer(orderService));

        producerThread.start();
        consumerThread.start();
    }
}

class Order {
    private int orderId;
    private String shoeType;

    private int quantity;


    public Order(int orderId, String shoeType, int quantity) {
        this.orderId = orderId;
        this.shoeType = shoeType;
        this.quantity = quantity;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getShoeType() {
        return shoeType;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", shoeType='" + shoeType + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}

class OrderService {



    private int MAX_ORDER_CAPACITY = 5;
    private int MIN_ORDER_CAPACITY = 0;

    private Queue<Order> orders = new ArrayDeque<>();

    OrderService(){

    }

    public OrderService(int minOrderQuantity, int maxOrderQuantity) {
        this.MAX_ORDER_CAPACITY = maxOrderQuantity;
        this.MIN_ORDER_CAPACITY = minOrderQuantity;
    }

    public  void receiveOrder(Order order){

        synchronized (orders){
            while (orders.size() == MAX_ORDER_CAPACITY){
                try {
                    orders.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            orders.add(order);
            orders.notifyAll();
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Order sent for processing:: " + order);
        }

    }

    public  void fulfillOrder(){
        synchronized (orders){
            while (orders.size() == MIN_ORDER_CAPACITY){
                try {
                    orders.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            var order = orders.poll();
            orders.notifyAll();
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Order fulfilled:: " + order);
        }
    }

}

class OrderProducer implements Runnable {

    private OrderService orderService;

    public OrderProducer(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public void run() {
        var random = new Random();
        while (true){
            var order = new Order(random.nextInt(1, 10000), "Sandals", random.nextInt(1, 20) );
            this.orderService.receiveOrder(order);
        }
    }
}

class OrderConsumer implements Runnable {

    private OrderService orderService;

    public OrderConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public void run() {
        while (true){
            this.orderService.fulfillOrder();
        }
    }
}
