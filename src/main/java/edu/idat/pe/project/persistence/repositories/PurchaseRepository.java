package edu.idat.pe.project.persistence.repositories;

import edu.idat.pe.project.persistence.entities.PurchaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<PurchaseEntity, Long> {
    List<PurchaseEntity> findByUsuario_NombreUsuario(String nombreUsuario);

    @Query(value = "SELECT c.id AS compra_id, c.cantidad, c.deleted, c.precio, c.fecha_compra, c.total, c.usuario_id, " +
                   "f.id AS vuelo_id, f.capacidad, f.deleted, f.hora_salida, f.duracion, f.imagen, f.precio, f.itinerario_id " +
                   "FROM compras c " +
                   "JOIN purchase_vuelo pv ON c.id = pv.purchase_id " +
                   "JOIN vuelo f ON pv.vuelo_id = f.id " +
                   "WHERE c.deleted = true", nativeQuery = true)
    List<Object[]> findDeletedPurchases();



    @Modifying
    @Query("UPDATE PurchaseEntity p SET p.deleted = false WHERE p.id = :id AND p.deleted = true")
    void restorePurchaseById(@Param("id") Long id);

    List<PurchaseEntity> findByUsuario_NombreUsuarioAndDeleted(String nombreUsuario, boolean b);


    List<PurchaseEntity> findByDeletedTrue();
}
