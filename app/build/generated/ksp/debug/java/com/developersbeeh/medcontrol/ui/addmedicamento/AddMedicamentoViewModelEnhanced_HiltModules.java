package com.developersbeeh.medcontrol.ui.addmedicamento;

import androidx.lifecycle.ViewModel;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityRetainedComponent;
import dagger.hilt.android.components.ViewModelComponent;
import dagger.hilt.android.internal.lifecycle.HiltViewModelMap;
import dagger.hilt.codegen.OriginatingElement;
import dagger.multibindings.IntoMap;
import dagger.multibindings.LazyClassKey;
import javax.annotation.processing.Generated;

@Generated("dagger.hilt.android.processor.internal.viewmodel.ViewModelProcessor")
@OriginatingElement(
    topLevelClass = AddMedicamentoViewModelEnhanced.class
)
public final class AddMedicamentoViewModelEnhanced_HiltModules {
  private AddMedicamentoViewModelEnhanced_HiltModules() {
  }

  @Module
  @InstallIn(ViewModelComponent.class)
  public abstract static class BindsModule {
    private BindsModule() {
    }

    @Binds
    @IntoMap
    @LazyClassKey(AddMedicamentoViewModelEnhanced.class)
    @HiltViewModelMap
    public abstract ViewModel binds(AddMedicamentoViewModelEnhanced vm);
  }

  @Module
  @InstallIn(ActivityRetainedComponent.class)
  public static final class KeyModule {
    private KeyModule() {
    }

    @Provides
    @IntoMap
    @LazyClassKey(AddMedicamentoViewModelEnhanced.class)
    @HiltViewModelMap.KeySet
    public static boolean provide() {
      return true;
    }
  }
}
