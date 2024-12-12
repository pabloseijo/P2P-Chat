# P2P-Chat

P2P-Chat is a distributed instant messaging system developed in Java using Java RMI (Remote Method Invocation). It supports direct client-to-client communication, real-time notifications, and dynamic management of users and friend groups.

## Features

- **Direct Client Communication**: Enables direct messaging between clients without the need for an intermediary server.
- **Real-Time Notifications**: Provides immediate updates for incoming messages and user status changes.
- **Dynamic User and Friend Group Management**: Allows users to manage their contacts and friend groups dynamically.

## Installation

1. Clone the repository.

2. Navigate to the project directory.

3. Build the project using Maven.

## Usage

1. Start the RMI registry.

2. Run the server.

3. Run the client.

4. Start chatting between clients connected to the server.

## Project Structure

```
P2P-Chat/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── com/
│   │   │   │   ├── p2pchat/
│   │   │   │       ├── server/       # Server-side implementation
│   │   │   │       ├── client/       # Client-side implementation
│   │   │   │       ├── utils/        # Utility classes
│   └── test/                         # Unit tests
│
├── pom.xml                           # Maven configuration file
└── README.md                         # Project documentation
```

The project is organized into the following main components:

- **Server**: Handles connections and message routing.
- **Client**: Provides the user interface for messaging.
- **Utilities**: Contains helper classes and shared logic.

## Requirements

- Java 8 or higher
- Apache Maven

## License

This project is licensed under the MIT License.

## Contributing

Contributions are welcome! Please open an issue or submit a pull request to propose changes.

## Contact

For any questions or suggestions, feel free to reach out or open an issue on the repository.
