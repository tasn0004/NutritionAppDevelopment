//
//  Settings.swift
//  NutritionAppiOS
//
//  Created by Tanisa Tasneem on 2023-07-14.
//

import SwiftUI

struct Settings: View {
    
    @State var screenHeight = 0.0
    @State var screenWidth = 0.0
    
    var body: some View {
        
        ZStack() {
            /*
                Set background colour and grab dimensions of screen by storing in state variables
             */
            GeometryReader { geometry in
                ComponentColours.backgroundPrimary.ignoresSafeArea()
                    .onAppear() {
                        self.screenHeight = geometry.size.height
                        self.screenWidth = geometry.size.width
                    }
            }
            
            /*
                Main VStack
             */
            VStack(spacing: 0) {
                /*
                    Profile picture and username header
                 */
                HStack() {
                    ZStack {
                        Circle()
                            .fill(Color.yellow)
                            .frame(width: 0.25*screenWidth, height:  0.20*screenHeight)

                        Text("User \nProfile")
                            .font(Font.system(size: 0.045*screenWidth, weight: .bold))
                       
                    }
                    .padding(.horizontal)
                    
                    Text("User Name")
                        .foregroundColor(Color("neonGreen"))
                        .font(Font.system(size: 0.045*screenWidth, weight: .bold))
                    Spacer()
                }
                
                /*
                    Divider line
                 */
                Rectangle()
                    .fill(Color("neonGreen"))
                    .frame(width: screenWidth, height: 0.0015*screenHeight)
                    .padding(.vertical)
            
                    /*
                        Settings options VStack
                     */
                    VStack(spacing: 0.03*screenHeight) {
                        
                        createSettingOption("profile", "My Account", "Edit your profile information")
                        
                        createSettingOption("bookmark", "Favorite", "Your favorite recipes")
                        
                        createSettingOption("payment", "Payment", "Manage your subscription")
                        
                        createSettingOption("promo", "Promotions", "Promo code that applies to your payment")
                        
                        createSettingOption("list", "Diet Preferences", "Edit your diet preferences")
                        
                        createSettingOption("health", "Health Concerns", "Edit your health concerns")
                        
                        createSettingOption("info", "About", "About us")
                        
                        createSettingOption("privacy", "Privacy", "Read terms & conditions")
                        
                        createSettingOption("exit", "Log Out", "Log out of your profile")
                        
                    }//Settings VStack
                    .padding()
                Spacer()
            }//Main VStack
        }//Zstack
    }//body
    
    /*
        Takes in required data to build and orient a setting option view on the settings page
     */
    func createSettingOption(_ imageName: String, _ textStringTop: String, _ textStringBottom: String) -> some View {
        HStack(spacing: 0.03*screenWidth) {
            Image(imageName)
                .resizable()
                .scaledToFit()
                .frame(width: 0.06*screenWidth, height: 0.03*screenHeight)
            
            VStack(alignment: .leading) {
                Text(textStringTop)
                    .foregroundColor(Color("neonGreen"))
                    .font(.system(size: 0.038*screenWidth, weight: .light))
                
                Text(textStringBottom)
                    .foregroundColor(.gray)
                    .font(.system(size: 0.034*screenWidth, weight: .light))
            }
            Spacer()
        }
    }
}


        
struct Settings_Previews: PreviewProvider {
    static var previews: some View {
        Settings()
    }
}
