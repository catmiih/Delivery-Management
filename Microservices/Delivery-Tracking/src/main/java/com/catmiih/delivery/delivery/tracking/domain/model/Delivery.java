package com.catmiih.delivery.delivery.tracking.domain.model;

import com.catmiih.delivery.delivery.tracking.domain.exception.DomainException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Setter(AccessLevel.PRIVATE)
@Getter
public class Delivery {

    @Id
    @EqualsAndHashCode.Include
    private UUID id;

    private UUID courierId;

    private DeliveryStatus status;

    private OffsetDateTime placedAt;
    private OffsetDateTime assignedAt;
    private OffsetDateTime expectDeliveryAt;
    private OffsetDateTime fulfilledAt;

    private BigDecimal distanceFee;
    private BigDecimal courierPayout;
    private BigDecimal totalCost;

    private Integer totalItems;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "zipCode", column = @Column(name="sender_zip_code")),
            @AttributeOverride(name = "street", column = @Column(name="sender_street")),
            @AttributeOverride(name = "number", column = @Column(name="sender_number")),
            @AttributeOverride(name = "complement", column = @Column(name="sender_complement")),
            @AttributeOverride(name = "name", column = @Column(name="sender_name")),
            @AttributeOverride(name = "phone", column = @Column(name="sender_phone"))
    })
    private ContactPoint sender;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "zipCode", column = @Column(name="recipient_zip_code")),
            @AttributeOverride(name = "street", column = @Column(name="recipient_street")),
            @AttributeOverride(name = "number", column = @Column(name="recipient_number")),
            @AttributeOverride(name = "complement", column = @Column(name="recipient_complement")),
            @AttributeOverride(name = "name", column = @Column(name="recipient_name")),
            @AttributeOverride(name = "phone", column = @Column(name="recipient_phone"))
    })
    private ContactPoint recipient;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "delivery")
    private List<Item> items = new ArrayList<>();

    public static Delivery draft(){
        Delivery delivery = new Delivery();
        delivery.setId(UUID.randomUUID());
        delivery.setStatus(DeliveryStatus.DRAFT);
        delivery.setTotalItems(0);
        delivery.setTotalCost(BigDecimal.ZERO);
        delivery.setCourierPayout(BigDecimal.ZERO);
        delivery.setDistanceFee(BigDecimal.ZERO);

        return delivery;
    }

    public UUID addItem(String name, int quantity){
        Item item = Item.brandNew(name,quantity, this);
        items.add(item);
        calculateToalItems();
        return item.getId();
    }

    public void removeItem(UUID itemId){
        items.removeIf(item -> item.getId().equals(itemId));
        calculateToalItems();
    }

    public void changeItemQuantity(UUID itemId, int quantity){
        Item item = getItems().stream().filter(i->i.getId().equals(itemId)).findFirst().orElseThrow();

        item.setQuantity(quantity);
        calculateToalItems();
    }

    public void removeItems(){
        items.clear();
        calculateToalItems();
    }

    public void editPreparationDetails(PreparationDetails details){
        verifyIfCanBeEdited();
        setSender(details.getSender());
        setRecipient(details.getRecipient());
        setDistanceFee(details.getDistanceFee());
        setCourierPayout(details.getCourierPayout());
        setExpectDeliveryAt(OffsetDateTime.now().plus(details.getExpectedDeliveryTime()));
        setTotalCost(this.getDistanceFee().add(this.courierPayout));
    }

    public void place(){
        verifyIfCanBePlaced();
        changeStatusTo(DeliveryStatus.WAITING_FOR_COURIER);
        this.setPlacedAt(OffsetDateTime.now());
    }

    public void pickUp(UUID courierId){
        this.setCourierId(courierId);
        changeStatusTo(DeliveryStatus.IN_TRANSIT);
        this.setAssignedAt(OffsetDateTime.now());
    }

    public void markAsDelivered(){
        changeStatusTo(DeliveryStatus.DELIVERED);
        this.setFulfilledAt(OffsetDateTime.now());
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(this.items);
    }

    private void calculateToalItems(){
        setTotalItems(getItems().stream().mapToInt(Item::getQuantity).sum());
    }

    private void verifyIfCanBePlaced(){
        if(!isFilled()){
            throw new DomainException();
        }
        if(!getStatus().equals(DeliveryStatus.DRAFT)){
            throw new DomainException();
        }
    }

    private void verifyIfCanBeEdited(){
        if(!getStatus().equals(DeliveryStatus.DRAFT)){
            throw new DomainException();
        }
    }

    private boolean isFilled(){
        return this.getSender() != null
                && this.getRecipient()!= null
                && this.totalCost != null;
    }

    private void changeStatusTo(DeliveryStatus newStatus){
        if(newStatus != null & this.getStatus().canChangeTo(newStatus)){
            this.setStatus(newStatus);
        }else{
            throw new DomainException("Impossível transacionar de "+this.getStatus()+" para "+newStatus);
        }
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class PreparationDetails{
        private ContactPoint sender;
        private ContactPoint recipient;

        private BigDecimal distanceFee;
        private BigDecimal courierPayout;

        private Duration expectedDeliveryTime;
    }
}
