# SpringBlog: A Modern Full-Stack Blogging Platform

A full-featured blogging platform built from the ground up, featuring a powerful Spring Boot backend and a dynamic and responsive vanilla JavaScript frontend.

## Key Features

### Post Management
- **Full CRUD Operations:** Create, Read, Update, and Delete posts.
- **Rich Text Editing:** A beautiful Markdown editor (EasyMDE) for writing and editing posts with real-time preview.
- **Pagination:** Infinite scroll-style "Load More" button to seamlessly load older posts.
- **Tagging System:** Posts can be tagged, and tags are displayed on post cards for easy categorization.
- **Search Functionality:** A powerful, real-time search bar in the header to find posts by keywords.

### User & Authentication
- **JWT-Based Security:** Secure user authentication and authorization using JSON Web Tokens.
- **User Accounts:** Users can sign up, log in, and log out.
- **Profile Management:** Logged-in users can view and edit their profile information (email, bio, website).
- **"My Posts" Page:** Users can view a dedicated page listing all the posts they have created, with quick access to edit or delete them.

### Engagement & Interaction
- **Commenting System:** Users can add comments to posts.
- **Comment Moderation:** Users can edit or delete their own comments.
- **Confirmation Modals:** Safe and user-friendly confirmation dialogs for destructive actions like deleting posts or comments.

## Technologies Used

| Area    | Technologies                                                              |
|---------|---------------------------------------------------------------------------|
| **Backend**   | Java, Spring Boot, Spring Security, Spring Data JPA, Maven, JWT, H2/MySQL |
| **Frontend**  | HTML5, CSS3, JavaScript (ES6+), jQuery, AJAX, Font Awesome, EasyMDE      |
| **DevOps**    | GitHub Actions, Docker                                     |

## Getting Started

To get a local copy up and running, follow these simple steps.

### Prerequisites

- Java 21
- Maven 3+
- A running SQL database (like MySQL or PostgreSQL), or use the default H2 in-memory database.

### Backend Setup

1.  **Clone the repository:**
    ```sh
    git clone https://github.com/Anss1/Full_stack_Blog_web_app.git
    ```
2.  **Configure the database:**
    Open `src/main/resources/application.properties` and update the `spring.datasource` properties to point to your local database if you are not using the default H2 database.
3.  **Run the application:**
    ```sh
    ./mvnw spring-boot:run
    ```
    The backend API will be running at `http://localhost:8080`.

### Frontend Setup

1.  The frontend is composed of static files (`.html`, `.css`, `.js`).
2.  You can open the `index.html` file from the `frontend` directory directly in your browser, or serve it with a simple web server. For the best experience (to avoid CORS issues), it's recommended to use a lightweight server.
3.  If you have Python installed:
    ```sh
    cd frontend
    python -m http.server <Port>
    ```
    The frontend will be accessible at `http://localhost:<Port>`.

## API Endpoints

A brief overview of the main API endpoints available in the backend.

| Method | Endpoint                       | Description                               |
|--------|--------------------------------|-------------------------------------------|
| `GET`    | `/api/v1/posts`                | Get a paginated list of all posts.        |
| `GET`    | `/api/v1/posts/{id}`           | Get a single post by its ID.              |
| `POST`   | `/api/v1/posts`                | Create a new post (Auth required).        |
| `PUT`    | `/api/v1/posts/{id}`           | Update an existing post (Auth required).  |
| `DELETE` | `/api/v1/posts/{id}`           | Delete a post (Auth required).            |
| `GET`    | `/api/v1/posts/search`         | Search for posts by a keyword.            |
| `POST`   | `/api/v1/auth/login`           | Log in a user and receive a JWT.          |
| `POST`   | `/api/v1/auth/signup`          | Register a new user.                      |
| `GET`    | `/api/v1/profiles/{username}`  | Get a user's profile information.         |

## Future Improvements

- **Clickable Tags:** Implement a page to show all posts associated with a specific tag.
- **Role-Based Access:** Introduce `ADMIN` roles with special privileges.
- **Frontend Framework:** Migrate the vanilla JavaScript frontend to a modern framework like React or Angular.

## License

Distributed under the MIT License. See `LICENSE` for more information.  
