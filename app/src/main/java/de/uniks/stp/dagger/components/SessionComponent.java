package de.uniks.stp.dagger.components;

import dagger.BindsInstance;
import dagger.Subcomponent;
import de.uniks.stp.dagger.modules.SessionModule;
import de.uniks.stp.model.User;

import javax.inject.Named;

@Subcomponent(modules = SessionModule.class)
public interface SessionComponent {

    @Subcomponent.Builder
    interface Builder {

        @BindsInstance
        Builder userKey(@Named("userKey") String userKey);

        @BindsInstance
        Builder currentUser(@Named("currentUser") User currentUser);

        SessionComponent build();
    }
}
