# Bistro - Client-Server Restaurant Management System 🍽️

A robust client-server simulation for managing restaurant orders, designed to handle multiple concurrent clients and persist data securely.

## 🚀 Overview
This project simulates a real-time restaurant management environment using a **Client-Server architecture**. It allows multiple clients (e.g., waiters or customers) to send orders to a central server concurrently. The server processes these orders in parallel and stores the relevant customer and order data in a relational database, ensuring full data integrity.

## ✨ Key Features
* **Client-Server Architecture:** Reliable network communication between the restaurant's clients and the central server.
* **Concurrency & Multithreading:** The server is engineered to handle multiple incoming orders simultaneously without blocking, ensuring parallel order processing.
* **Database Integration:** Seamless connection to a database for persisting customer details, order histories, and menu items.
* **Data Integrity:** Strict SQL queries and transaction handling to maintain a consistent state of the restaurant's data.

## 🛠️ Technologies & Tools
* **Language:** Java (JDK 8+)
* **Database:** MySQL
* **Networking:** Java Sockets (TCP/IP)
* **Concurrency:** Java Multithreading (Runnable / Thread Pools)
* **Database Connectivity:** JDBC API

## ⚙️ Getting Started

### Prerequisites
1. Ensure you have **Java** installed on your machine.
2. Install **MySQL Server** and have it running locally or remotely.

### Database Setup
1. Create a local MySQL database for the project (e.g., `bistro_db`).
2. Update the database connection credentials (URL, Username, Password) inside the server's configuration or database connection class.
3. Run the provided SQL scripts (if available) to generate the required tables (`customers`, `orders`, etc.).

### Running the Application
1. **Start the Server:** Run the main server class first to start listening for incoming connections on the designated port.
2. **Start the Client(s):** Run one or more instances of the client application to connect to the server and simulate sending orders.

## 📂 Repository Structure (Assignment 3)
* `src/server/` - Contains the server-side logic, multithreading handlers, and database connection utilities.
* `src/client/` - Contains the client-side UI/Console logic for sending requests to the server.
