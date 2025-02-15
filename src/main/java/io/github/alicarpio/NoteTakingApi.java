package io.github.alicarpio;

import io.github.alicarpio.domain.data.repositories.PsqlNoteRepository;
import io.github.alicarpio.domain.data.repositories.PsqlUserRepository;
import io.github.alicarpio.domain.use_cases.*;
import io.github.alicarpio.domain.validations.UserValidator;
import io.github.alicarpio.repositories.NoteRepository;
import io.github.alicarpio.repositories.UserRepository;
import io.github.alicarpio.routes.JwtMiddleware;
import io.github.alicarpio.routes.NoteRoutes;
import io.github.alicarpio.routes.UserRoutes;
import io.github.alicarpio.utils.CorsFilter;
import io.github.alicarpio.utils.HibernateUtil;
import spark.Spark;

public class NoteTakingApi {
    public static void main(String[] args) {
        Spark.port(5315);
        HibernateUtil.initialize();

        UserRepository userRepository = new PsqlUserRepository();
        NoteRepository noteRepository = new PsqlNoteRepository();

        UserRegistrationUseCase userRegistrationUseCase = new UserRegistrationUseCase(userRepository, new UserValidator());
        UserLogInUseCase userLogInUseCase = new UserLogInUseCase(userRepository);
        CreateNoteUseCase createNoteUseCase = new CreateNoteUseCase(noteRepository, userRepository);
        ViewAllNotesUseCase viewAllNotesUseCase = new ViewAllNotesUseCase(noteRepository, userRepository);
        DeleteNoteUseCase deleteNoteUseCase = new DeleteNoteUseCase(noteRepository);
        RefreshTokenUseCase refreshTokenUseCase = new RefreshTokenUseCase(userRepository);

        UserRoutes userRoutes = new UserRoutes(userRegistrationUseCase, userLogInUseCase, refreshTokenUseCase);
        NoteRoutes noteRoutes = new NoteRoutes(createNoteUseCase, viewAllNotesUseCase, deleteNoteUseCase);

        CorsFilter corsFilter = new CorsFilter();
        corsFilter.apply();

        JwtMiddleware.setupJwtAuth();

        userRoutes.setupRoutes();

        Spark.path("/api/auth", () -> {
            noteRoutes.setupRoutes();
        });

        Spark.init();
        System.out.println("Spark is running on port 5315.");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Spark.stop();
            HibernateUtil.shutdown();
        }));
    }
}
