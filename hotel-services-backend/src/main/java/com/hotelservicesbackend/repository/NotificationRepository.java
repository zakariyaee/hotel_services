package com.hotelservicesbackend.repository;

import com.hotelservicesbackend.entity.Notification;
import com.hotelservicesbackend.entity.enums.StatutNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUtilisateur_IdUtilisateur(Long idUtilisateur);
    List<Notification> findByDemande_IdDemande(Long idDemande);
    List<Notification> findByStatutEnvoi(StatutNotification statutEnvoi);
}
