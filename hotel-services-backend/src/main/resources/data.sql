-- ========================================================
-- DONNEES DE TEST POUR HOTEL-SERVICES
-- ========================================================

-- Utilisateurs pour l'authentification existante
INSERT IGNORE INTO app_user (id, username, password, role) VALUES (1, '412', '123456', 'GUEST');
INSERT IGNORE INTO app_user (id, username, password, role) VALUES (2, 'client1', 'client123', 'CLIENT');
INSERT IGNORE INTO app_user (id, username, password, role) VALUES (3, 'staff1', 'staff123', 'STAFF');
INSERT IGNORE INTO app_user (id, username, password, role) VALUES (4, 'admin', 'admin123', 'ADMIN');

-- Departements
INSERT IGNORE INTO departement (id_departement, nom, description, horaires_ouverture) VALUES
(1, 'Restauration & Room Service', 'Service des repas, petits-déjeuners et boissons en chambre.', '24h/24 - 7j/7'),
(2, 'Housekeeping & Gouvernance', 'Entretien des chambres, blanchisserie et commodités.', '07:00 - 23:00'),
(3, 'Conciergerie & Accueil', 'Assistance bagages, réservations extérieures et transports.', '24h/24 - 7j/7'),
(4, 'Maintenance & Technique', 'Dépannage climatisation, électricité et équipements.', '08:00 - 20:00');

-- Services intra-hôteliers
INSERT IGNORE INTO service (id_service, nom, description, duree_estimee, disponibilite, image_url, id_departement) VALUES
(1, 'Room Service Gourmet', 'Plateaux repas chauds, collations et boissons fraîches servis directement dans votre chambre.', 35, 1, 'https://images.unsplash.com/photo-1544025162-d76694265947?w=600', 1),
(2, 'Petit-Déjeuner Continental', 'Assortiment de viennoiseries, fruits frais, café/thé et jus d''orange pressé en chambre.', 20, 1, 'https://images.unsplash.com/photo-1533089860892-a7c6f0a88666?w=600', 1),
(3, 'Ménage & Renouvellement Linge', 'Rafraîchissement complet de la suite et changement du linge de bain.', 30, 1, 'https://images.unsplash.com/photo-1581578731548-c64695cc6952?w=600', 2),
(4, 'Blanchisserie Express', 'Nettoyage à sec et repassage soigné de vos vêtements sous 3 heures.', 180, 1, 'https://images.unsplash.com/photo-1517677208171-0bc6725a3e60?w=600', 2),
(5, 'Navette Aéroport & Chauffeur', 'Véhicule avec chauffeur privé pour vos transferts vers la gare ou l''aéroport.', 45, 1, 'https://images.unsplash.com/photo-1549465220-1a8b9238cd48?w=600', 3),
(6, 'Réveil Téléphonique & Café', 'Appel personnalisé à l''heure souhaitée avec livraison d''un expresso.', 10, 1, 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=600', 3),
(7, 'Assistance Technique Chambre', 'Intervention rapide pour climatisation, télévision, coffre-fort ou Wi-Fi.', 25, 1, 'https://images.unsplash.com/photo-1581092921461-eab62e97a780?w=600', 4);

-- Chambres
INSERT IGNORE INTO chambre (id_chambre, num_chambre, type_chambre, etage, capacite, statut) VALUES
(1, '101', 'Suite Junior Deluxe', 1, 2, 'DISPONIBLE'),
(2, '102', 'Chambre Supérieure Vue Jardin', 1, 2, 'DISPONIBLE'),
(3, '201', 'Suite Royale Prestige', 2, 4, 'OCCUPEE'),
(4, '202', 'Chambre Exécutive', 2, 2, 'DISPONIBLE'),
(5, '301', 'Penthouse Panoramique', 3, 4, 'MAINTENANCE');

-- Utilisateurs avec héritage JOINED
INSERT IGNORE INTO utilisateur (id_utilisateur, nom, prenom, email, mot_de_passe, tel, role) VALUES
(1, 'Allouch', 'Zakaria', 'zakaria@hotel.com', 'client123', '+33611223344', 'CLIENT'),
(2, 'Dupont', 'Claire', 'claire.reception@hotel.com', 'staff123', '+33622334455', 'STAFF'),
(3, 'Moreau', 'Lucas', 'lucas.agent@hotel.com', 'staff123', '+33633445566', 'STAFF'),
(4, 'Benali', 'Yasmine', 'yasmine.agent@hotel.com', 'staff123', '+33644556677', 'STAFF');

-- Client (id_utilisateur = 1)
INSERT IGNORE INTO client (id_utilisateur, reservation_statut, langue_preferee, preference_notification) VALUES
(1, 1, 'FR', 1);

-- Employes (id_utilisateur = 2, 3, 4)
INSERT IGNORE INTO employe (id_utilisateur, poste, heure_debut, heure_fin, id_departement) VALUES
(2, 'Chef de Réception', '2026-10-13 08:00:00', '2026-10-13 16:00:00', 3),
(3, 'Agent Room Service', '2026-10-13 12:00:00', '2026-10-13 20:00:00', 1),
(4, 'Gouvernante d''étage', '2026-10-13 07:00:00', '2026-10-13 15:00:00', 2);

-- Réceptionniste
INSERT IGNORE INTO receptionniste (id_utilisateur, poste_reception, extension_telephonique) VALUES
(2, 'Accueil Hall Principal', '1001');

-- Agents
INSERT IGNORE INTO agent (id_utilisateur, disponible) VALUES
(3, 1),
(4, 1);

-- Réservation pour le client 1 dans la chambre 3 (Suite Royale Prestige)
INSERT IGNORE INTO reservation (id_reservation, date_debut, date_fin, statut, type_reservation, id_chambre, id_client) VALUES
(1, '2026-10-10', '2026-10-20', 'EN_COURS', 'SEJOUR_PRESTIGE', 3, 1);

-- Demandes initiales
INSERT IGNORE INTO demande (id_demande, reference_demande, date_creation, priorite, statut, temps_traitement_estime, id_client, id_service, id_receptionniste) VALUES
(1, 'DEM-2026-001', '2026-10-13 10:30:00', 'HAUTE', 'EN_COURS', 35, 1, 1, 2),
(2, 'DEM-2026-002', '2026-10-13 11:15:00', 'NORMALE', 'EN_ATTENTE', 30, 1, 3, NULL);
