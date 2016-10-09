package module

import dao.services._

class EQModule extends CoreModule {

  override def configure(): Unit = {

    bindSingleton[FeatureService, FeatureServiceImpl]
    bindSingleton[GeometryService, GeometryServiceImpl]
  }
}
