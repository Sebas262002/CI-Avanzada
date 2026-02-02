package ec.edu.espe.buildtestcl.service;

import ec.edu.espe.buildtestcl.dto.WalletResponse;
import ec.edu.espe.buildtestcl.model.Wallet;
import ec.edu.espe.buildtestcl.repository.WalletRepository;

import java.util.Optional;

public class WalletService {
    private final WalletRepository walletRepository;
    private final RiskClient riskClient;

    public WalletService(WalletRepository walletRepository, RiskClient riskClient) {
        this.walletRepository = walletRepository;
        this.riskClient = riskClient;
    }


    //Crear una cuenta si cumple con las reglas del negocio
    public WalletResponse createWallet(String ownerEmail, double initialBalance) {
        //Validaciones de casos negativos
        if (ownerEmail == null || ownerEmail.isEmpty() || !ownerEmail.contains("@")) {
            throw new IllegalArgumentException("Invalid email address");
        }
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }

        //Regla de negocio: usuario bloqueado
        if (riskClient.isBloqued(ownerEmail)) {
            throw new IllegalStateException("User blocked");
        }

        //Regla de negocio: no duplicar cuenta por email
        if (walletRepository.existsByOwnerEmail(ownerEmail)) {
            throw  new IllegalStateException("User already exists");
        }

        Wallet wallet = new Wallet(ownerEmail, initialBalance);
        Wallet save = walletRepository.save(wallet);

        return new WalletResponse(save.getId(), save.getBalance());
    }

    //Depositar dinero
    public double deposit(String walletId, double amount){
        //Validaciones
        if (amount < 0){
            throw new IllegalStateException("Amount cannot be negative");
        }
        Optional<Wallet> found = walletRepository.findById(Long.valueOf(walletId));
        if (found.isEmpty()){
            throw  new IllegalStateException("Wallet not found");
        }

        Wallet wallet = found.get();
        wallet.deposit(amount);

        //Persistimos el nuevo saldo
        walletRepository.save(wallet);

        return  wallet.getBalance();
    }

}
