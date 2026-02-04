// java
package ec.edu.espe.buildtestcl;

import ec.edu.espe.buildtestcl.dto.WalletResponse;
import ec.edu.espe.buildtestcl.model.Wallet;
import ec.edu.espe.buildtestcl.repository.WalletRepository;
import ec.edu.espe.buildtestcl.service.RiskClient;
import ec.edu.espe.buildtestcl.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class WalletServiceTest {
    private WalletRepository walletRepository;
    private WalletService walletService;
    private RiskClient riskClient;

    @BeforeEach
    public void setUp(){
        walletRepository = Mockito.mock(WalletRepository.class);
        riskClient = Mockito.mock(RiskClient.class);
        walletService = new WalletService(walletRepository, riskClient);
    }

    @Test
    void createWallet_validData_shouldSaveAndReturnResponse(){
        //Arrange
        String email = "kslechon@espe.edu.ec";
        double initial = 100.0;

        when(walletRepository.existsByOwnerEmail(email)).thenReturn(Boolean.FALSE);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArguments()[0]);

        //Act
        WalletResponse response = walletService.createWallet(email, initial);

        //Assert
        assertNotNull(response.getWalletId());
        assertEquals(100.0, response.getBalance());

        verify(riskClient).isBloqued(email);
        verify(walletRepository).save(any(Wallet.class));
        verify(walletRepository).existsByOwnerEmail(email);
    }

    @Test
    void createWallet_invalidEmail_shouldThrow_andNotCallDependencies(){
        //Arrenge
        String invalidEmail = "kslechon-espe.edu.ec";

        //Act + Assert
        assertThrows(IllegalArgumentException.class, () -> walletService.createWallet(invalidEmail, 50.0));

        //No debe llamar a ninguna dependencia porque falta la validacion
        verifyNoInteractions(walletRepository, riskClient);
    }

    @Test
    void deposit_walletNotFound_shouldThrow() {
        // Arrange
        String walletId = "1"; // id numérico para que Long.valueOf(...) no lance NumberFormatException
        when(walletRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act + Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> walletService.deposit(walletId, 60));

        assertEquals("Wallet not found", exception.getMessage());
        verify(walletRepository).findById(anyLong());
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    // java
    @Test
    void deposit_shouldUpdateBalance_andSave_UsingCaptor(){
        //Arrange
        Wallet wallet = new Wallet("kslechon@espe.edu.ec", 300.0);
        String walletId = "1"; // usar id numérico
        // devolver la wallet cuando se pida el id 1L
        when(walletRepository.findById(1L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));

        ArgumentCaptor<Wallet> captor = ArgumentCaptor.forClass(Wallet.class);

        //Act
        double newBalance = walletService.deposit(walletId, 300.0);

        //Assert
        assertEquals(600.0, newBalance);
        verify(walletRepository).save(captor.capture());
        Wallet saved = captor.getValue();
        assertEquals(600.0, saved.getBalance());
    }

    @Test
    void withdraw_insufucuentFunds_shuoldThrow_andNotSave(){
        // Arrange
        Wallet wallet = new Wallet("Luis@espe.edu.ec",300);
        String walletId = "2"; // usar id numérico como string

        when(walletRepository.findById(anyLong())).thenReturn(Optional.of(wallet));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                walletService.withdraw(walletId,500));

        assertEquals("Insufficient funds",exception.getMessage());
        verify(walletRepository,never()).save(any());

    }

    //probando ramas locales
    //Probando 2
}