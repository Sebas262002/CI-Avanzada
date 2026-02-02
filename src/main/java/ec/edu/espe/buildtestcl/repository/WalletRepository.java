package ec.edu.espe.buildtestcl.repository;


import ec.edu.espe.buildtestcl.model.Wallet;

import java.util.Optional;

public interface WalletRepository {
    Wallet save(Wallet wallet);

    Optional<Wallet> findById(Long id);
    boolean existsByOwnerEmail(String ownerEmail);

}
